var imgur = require('imgur');
var express = require('express');
var request = require('request');
var app = express();
var bodyParser = require('body-parser');
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

var google = require('google')
google.resultsPerPage = 5

var port = 7070;
var router = express.Router();

router.post('/imgurupload/', function(req, res) {
    uploadToImgur(req.body.b64File, res);
});

router.get('/google/', function(req, res) {
    googleSearch(req.query.q, res);
});

router.get('/imageinfo/', function(req, res){
    identifyImage(req.query.url, res);
})

app.use('/api', router);
app.listen(port);
console.log('Server started on port: ' + port);

function uploadToImgur(file, res){
    imgur.uploadBase64(file)
    .then(function (json) {
        console.log(json.data.link);
        res.json({link: json.data.link});
    })
    .catch(function (err) {
        console.error(err.message);
        res.send(err);
    });
}

function identifyImage(imgUrl, res){
    var options = {
      uri: 'http://localhost:5000/search',
      method: 'POST',
      json: {
        "image_url": imgUrl
      }
    };
    request(options, function (error, response, body) {
        if(error) res.error(error);
        // res.send(body);
        if(!body.best_guess || body.best_guess==''){
            res.send({basic_info:body, purchase_info:[]});
            return;
        }
        googleSearch(body.best_guess+ " online buy", res, {basic_info:body});
    });
}

function googleSearch(q, r, body=null){
    var nextCounter = 0
    google(q, function (err, res){
        if (err){
            console.error(err);
            r.send(err);
        }
        if(body){
            body.purchase_info = res.links;
            r.send(body);
            return;
        }
        r.send(res.links);
    });
}