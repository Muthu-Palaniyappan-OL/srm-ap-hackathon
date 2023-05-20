const crypto = require('crypto')
const fs = require('fs')

const CAPublicKey = crypto.createPublicKey({key: Buffer.from(fs.readFileSync('public-key.pem'))})
const CAPrivateKey = crypto.createPrivateKey({key: Buffer.from(fs.readFileSync('private-key.pem'))})

function sign(key, data){
    hash = crypto.createHash('sha256').update(Buffer.from(data)).digest()
    signature = crypto.privateEncrypt({key: key}, hash)
    return Buffer.from(signature).toString('base64');
}

function verify(key, data, sign){
    let hash, signature;
    try {
        hash = crypto.createHash('sha256').update(Buffer.from(data, "base64")).digest().toString('base64')
        console.log('hash', hash)
        signature = crypto.publicDecrypt({key:key}, Buffer.from(sign, 'base64')).toString('base64')
        console.log('signature', signature)
    } catch (e) {
        console.error(e)
    }
    console.log(hash, signature)
    return hash == signature;
}

_public_key = `MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAp07cQMdsWfROzx9iYNt2Osq46fDWysmQQciQQ9A9++D0R/wRpArk8B4NV2WxoBtoXKRgBLHaqnRlqhZGkBR4BDDKO+Xn+ntKMBhDa08TeFh0UxWBNg8kKMIUn4/1R1anZeS4IO6e040ZCSfogaBKiqimjsmi8Rcu5CtZ5iVGhj9h4twutbffNGlvSmBjdgKVTdhZNhma7mHcBXhn4YVfLZJm44PUN3hbyOpbICrJSL+LIOgiDhTi/TUVatU1zD9UL0FWWZw9/kAC6Ys8mGHxfHPoQZxI332K1+Koudb8Xmk3P3NMadhIBhOiBlHL+QW0NJBwYVeEWyN7oahSTh6xiwIDAQAB`

const CAcert = Buffer.from(_public_key, "base64").toString("base64")+"."+sign(CAPrivateKey, Buffer.from(_public_key, "base64"))

// console.log(verify(CAPublicKey, "Muthu", sign(CAPrivateKey, "Muthu")))

module.exports = {
    sign,
    verify,
    CAPrivateKey,
    CAPublicKey,
    CAcert
}