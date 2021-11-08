const ResourceRecord = require('../common-classes/resource-record');
const { getCleanIpV6 } = require('./misc');
const TypedError = require('../common-classes/typed-error');
const { OPCODE } = require('../util/constants');

const DOMAIN_NAME_PATTERN = /^([A-Za-z]+[A-za-z\d-]*\.)*[A-Za-z]+[A-za-z\d-]*$/;
const IPV4_PATTERN = /^(\d{1,3}\.){3}\d{1,3}$/;

/**
 *
 * @param {Array<string>} questions
 * @return {{typedQuestions: Array<{ question: string, type: number }>, opCode: number}}
 */
function getQuestionsDescription(questions) {
  let opCode = -1;
  const typedQuestions = [];

  for (const question of questions) {
    let _opCode;
    if (DOMAIN_NAME_PATTERN.test(question)) {
      _opCode = OPCODE.standardQuery;
      typedQuestions.push({ question, type: null });
    } else if (IPV4_PATTERN.test(question)) {
      _opCode = OPCODE.inverseQuery;
      typedQuestions.push({ question, type: ResourceRecord.TYPE.ipv4 });
    } else {
      const cleanIpV6 = getCleanIpV6(question);
      if (cleanIpV6) {
        typedQuestions.push({ question, type: ResourceRecord.TYPE.ipv6 });
        _opCode = OPCODE.inverseQuery;
      } else {
        throw new TypedError(
          `Question ${question} is neither a domain name, ipv4 nor ipv6 address`,
          TypedError.TYPE.validation
        );
      }
    }

    if (opCode !== -1 && _opCode !== opCode) {
      throw new TypedError(
        'All questions must be of one kind, either standard or inverse',
        TypedError.TYPE.validation
      );
    }

    opCode = _opCode;
  }

  return { typedQuestions, opCode };
}

function getAField(type) {
  return type === ResourceRecord.TYPE.ipv6 ? 'aaaa' : 'a';
}

module.exports = {
  getQuestionsDescription,
  getAField
};