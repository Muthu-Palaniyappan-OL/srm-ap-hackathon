var express = require("express");
const req = require("express/lib/request");
const res = require("express/lib/response");
var path = require("path");

var app = express();

app.use(express.json());
app.use(express.urlencoded({ extended: false }));



const Web3 = require("web3");
const web3 = new Web3("http://127.0.0.1:8545");

const PRIVATE_KEY = "0xcfe5bff4f39e71c7ddc92a3d231e5548ad3d0347e5ee59541c028bab122e33fa";

const contractAddress = "0xd43ffCbcE8A4781fF87C36254025300Cdd3E42De"; // Address of the deployed ERC20 contract
const abi = [
  {
    inputs: [
      {
        internalType: "address",
        name: "receiver",
        type: "address",
      },
      {
        internalType: "uint256",
        name: "amount",
        type: "uint256",
      },
    ],
    name: "sendCoin",
    outputs: [
      {
        internalType: "bool",
        name: "sufficient",
        type: "bool",
      },
    ],
    stateMutability: "nonpayable",
    type: "function",
  },
  {
    inputs: [],
    stateMutability: "nonpayable",
    type: "constructor",
  },
  {
    anonymous: false,
    inputs: [
      {
        indexed: true,
        internalType: "address",
        name: "_from",
        type: "address",
      },
      {
        indexed: true,
        internalType: "address",
        name: "_to",
        type: "address",
      },
      {
        indexed: false,
        internalType: "uint256",
        name: "_value",
        type: "uint256",
      },
    ],
    name: "Transfer",
    type: "event",
  },
  {
    inputs: [
      {
        internalType: "address",
        name: "addr",
        type: "address",
      },
    ],
    name: "getBalance",
    outputs: [
      {
        internalType: "uint256",
        name: "",
        type: "uint256",
      },
    ],
    stateMutability: "view",
    type: "function",
  },
  {
    inputs: [
      {
        internalType: "address",
        name: "addr",
        type: "address",
      },
    ],
    name: "getBalanceInEth",
    outputs: [
      {
        internalType: "uint256",
        name: "",
        type: "uint256",
      },
    ],
    stateMutability: "view",
    type: "function",
  },
  {
    inputs: [],
    name: "getOwner",
    outputs: [
      {
        internalType: "address",
        name: "",
        type: "address",
      },
    ],
    stateMutability: "view",
    type: "function",
  },
  {
    inputs: [],
    name: "owner",
    outputs: [
      {
        internalType: "address",
        name: "",
        type: "address",
      },
    ],
    stateMutability: "view",
    type: "function",
  },
]; 

const contract = new web3.eth.Contract(abi, contractAddress);

/* GET home page. */



app.get('/test',(req,res)=>{
    let re = "";
    contract.methods.getOwner().call((error, result) => {
    if (error) {
      re = error;
      console.error(error);
    } else {
      re = result;
      console.log(result);
    }
  });
  console.log(re);
  res.send(200);
});

app.post('/balance',(req,res)=>{
    let re = "";
    let addr = req.body.addr;
    contract.methods.getBalance(addr).call((error, result) => {
    if (error) {
      re = error;
      console.error(error);
    } else {
      re = result;
      console.log(result);
    }
  });
  console.log(re);
  res.send(200);
});

app.post('/topup',(req,res)=>{
    let cliaddr = req.body.addr;
    let amount = req.body.amount;
    console.log(amount);
    //check if client is valid
    const fromAddress = '0x8C70BEb60f62A14D21350a794932bFF18Ef2c1Df';
const toAddress = cliaddr;


contract.methods.sendCoin(toAddress, amount).send({ from: fromAddress }, (err, transactionHash) => {
  if (err) {
    console.error(err);
  } else {
    console.log(`Transaction hash: ${transactionHash}`);
    const signedTransaction = web3.eth.accounts.signTransaction({
      to: contractAddress,
      gas: 500000,
      data: contract.methods.sendCoin(toAddress, amount).encodeABI(),
    }, PRIVATE_KEY);

  }
});
res.send(200);
})

var listener = app.listen(8080, function() {
  console.log("Listening on port " + listener.address().port);
});
