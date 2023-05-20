const Sequelize = require('sequelize')
const DataTypes = require('sequelize').DataTypes

const sequelize = new Sequelize('postgresql://piwesaf:v2_43X53_TP4KNe9EGhmqdvhp22vksRZ@db.bit.io:5432/piwesaf/payd_database', {
    host: 'bit.io',
    dialect: 'postgres',
    dialectOptions: {
      ssl: {
        require: true,
        rejectUnauthorized: false // This is optional depending on your server configuration
      }
    }
  });

try {
  (async () => {
    await sequelize.authenticate()
    console.log('Connection has been established successfully.');
  })()
} catch (error) {
  console.error('Unable to connect to the database:', error);
}

const User = sequelize.define('Users', {
  name: DataTypes.STRING,
  aadhar: DataTypes.STRING,
  gst: DataTypes.STRING,
  phonenumber: {
    type: DataTypes.STRING,
    primaryKey: true
  },
  password: DataTypes.STRING,
  status: DataTypes.STRING,
});

const Transaction = sequelize.define('Transactions', {
    uuid: {
      type: DataTypes.UUID,
      primaryKey: true
    },
    amount: DataTypes.INTEGER,
    senderName: DataTypes.TEXT,
    senderPhoneNumber: DataTypes.TEXT,
    senderCert: DataTypes.TEXT,
    senderSign: DataTypes.TEXT,
    receiverName: DataTypes.TEXT,
    receiverPhoneNumber: DataTypes.TEXT,
});

module.exports = {
    User,
    Transaction,
    sequelize
}