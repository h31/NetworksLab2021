require('dotenv').config();
const express = require('express');
const path = require('path');
const PORT = process.env.PORT;
const bodyParser = require('body-parser');
const app = express();


app.use(express.static(path.join(__dirname, 'public')));
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'public/server.html'));
});

app.post('/calculate', async (req, res) => {
  console.log("this is post");
  console.log(req.body);
  let arg1 = +req.body.arg1;
  let arg2 = +req.body.arg2;
  let op = req.body.op;
  let ans;
  console.log(typeof arg1);
  switch(op) {
  case '+':
    ans = arg1 + arg2;
    break;
  case '-':
    ans = arg1 - arg2;
    break;
  case '*':
    ans = arg1 * arg2;
    break;
  case '/':
    ans = arg1 / arg2;
    break;
  case '%':
    ans = arg1 % arg2;
    break;
  case '^':
    ans = arg1 ** arg2;
    break;
  case '√':
    ans = arg1 ** (1/arg2);
    break;
  }
  res.send(JSON.stringify(ans));
});

app.listen(PORT, (error) => {
  error ? console.log(error) : console.log(`Server started on port ${PORT}`);
});

