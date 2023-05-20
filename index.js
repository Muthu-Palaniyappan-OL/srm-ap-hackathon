const express = require('express')
const app = express()
const {User, Transaction, sequelize} = require('./models/index')
const {sign, verify, CAPrivateKey, CAPublicKey, CAcert} = require('./lib/core')
const {Op} = require('sequelize')
const cors = require('cors')

app.use(express.json())
app.use(cors.apply())

sequelize.sync()

app.post('/login',  async(req, res) => {
    user = await User.findOne({
        where: {
            phonenumber: req.body.phonenumber
        }
    })

    if (!user) {
        res.json({ message: 'No User Available'})
        return
    }
    
    if (user.status == "in") {
        res.json({ message: 'Status Aldready Logged In'})
        return
    }
    
    if (user.password != req.body.password) {
        res.json({ message: 'Wrong Password'})
        return
    }

    user.status = "in"
    user.save()

    res.json({ message: 'Success', signature: req.body.publickey+'.'+sign(CAPrivateKey, req.body.publickey) })
})

app.post('/signup',  async(req, res) => {
    user = await User.findOne({
        where: {
            phonenumber: req.body.phonenumber
        }
    })

    if (user) {
        res.json({ message: 'User aldready exist'})
        return
    }
    
    await User.create({
        name: req.body.name,
        aadhar: req.body.aadhar,
        gst: req.body.gst,
        phonenumber: req.body.phonenumber,
        password: req.body.password,
        status: "in",
    })

    res.json({ message: 'Success', signature: req.body.publickey+'.'+sign(CAPrivateKey, req.body.publickey) })
})

app.post('/logout',  async(req, res) => {
    user = await User.findOne({
        where: {
            phonenumber: req.body.phonenumber
        }
    })

    if (!user) {
        res.json({ message: 'No User Available'})
        return
    }

    user.status = "out"
    user.save()
    res.json({ message: 'Success' })
})

app.post('/syncTransactions',  async(req, res) => {
    user = await User.findOne({
        where: {
            phonenumber: req.body.phonenumber
        }
    })

    if (!user) {
        res.json({ message: 'No User Available'})
        return
    }

    await Transaction.bulkCreate(req.body.transactions, {
        updateOnDuplicate: ['uuid']
    })

    res.json(await Transaction.findAll({
        where: {
            [Op.or]: [
              { senderPhoneNumber: req.body.phonenumber },
              { receiverPhoneNumber: req.body.phonenumber }
            ]
          }
    }))
})


app.post('/getBalance',  async(req, res) => {
    user = await User.findOne({
        where: {
            phonenumber: req.body.phonenumber
        }
    })

    if (!user) {
        res.json({ message: 'No User Available'})
        return
    }

    transactions = await Transaction.findAll({
        where: {
            [Op.or]: [
              { senderPhoneNumber: req.body.phonenumber },
              { receiverPhoneNumber: req.body.phonenumber }
            ]
          }
    })

    let amount = 0

    transactions.forEach(element => {
        if (element.senderPhoneNumber == req.body.phonenumber) {
            amount -= element.amount;
        } else {
            amount += element.amount;
        }
    });

    res.json({
        amount: amount
    })
})


app.listen(process.env.PORT)