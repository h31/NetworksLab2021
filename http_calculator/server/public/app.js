'use strict'

let argElem1 = document.getElementById('arg1');
let opElem = document.getElementById('op');
let argElem2 = document.getElementById('arg2');
let buttonElem = document.getElementById('start');
let resElem = document.getElementById('res');

buttonElem.onclick = async function() {
  const arg1 = argElem1.value;
  const arg2 = argElem2.value;
  const op = opElem.value;
  if (((arg1 == "") || (arg2 == "") || (op == ""))) {
    alert("Ошибка ввода");
    return;
  }

  let ToSend = {
    arg1: arg1,
    arg2: arg2,
    op: op
  };

  let response = await fetch(`${location.href}calculate`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json;charset=utf-8'
    },
    body: JSON.stringify(ToSend)
  });

  let result = await response.text();
  if (result == 'null') {
    alert('Недопустимые аргументы');
    resElem.value = '';
    return;
  }
  resElem.value = result;
};
