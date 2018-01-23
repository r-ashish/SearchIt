var imgur = require('imgur');
var express = require('express');
var app = express();
var bodyParser = require('body-parser');
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

var port = process.env.PORT || 7070;
var router = express.Router();

router.get('/', function(req, res) {
    res.json({ message: 'hooray! welcome to our api!' });   
});

router.get('/imgurupload/', function(req, res) {
    uploadToImgur(req.param('b64File'), res);
});

app.use('/api', router);
app.listen(port);
console.log('Server started on port: ' + port);


function uploadToImgur(file, res){
    imgur.uploadBase64(imgurFavicon)
    .then(function (json) {
        console.log(json.data.link);
        res.send({link: json.data.link});
    })
    .catch(function (err) {
        console.error(err.message);
        res.send(err);
    });
}