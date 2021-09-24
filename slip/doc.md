# Slip
**Slip** is the serialization scheme <u>Toss-a-Message</u> supports.

Each **Slip** is a raw byte array, a concatenation of byte arrays representing the fields of the serialized object. The
byte arrays for fields, in turn, are concatenations of arrays representing slices of information about these fields.

The parts of any field, in order:
- *key*: the name of the field. Each of the special characters(`|`, `>` and `;`) should be escaped with an extra `>`. 
  Example: `'This is a fence > |-|-|;'` becomes `'This is a fence >> >|->|->|>;'`
- *key delimiter*: a single non-escaped `|`
- *type representation*: a single character representing the type of the data stored in the field (see the table below
  for more info)
- *size*: the length of the byte array holding the field value. <u>Pay attention: this is NOT the length of a String!
  The byte representation of a String does not always have the same size as that String. For example, the word
  'Münchhausen' has 11 letters, but is encoded with 12 bytes</u>
- *size delimiter*: a single non-escaped `|`
- *content*: the field value
- *field-ending*: a single non-escaped `;`

These Strings should be converted to byte arrays using utf8 encoding and concatenated to make a complete field.

<table>
    <thead>
        <tr>
            <th>Type</th>
            <th>Representation</th>
            <th>Extra rules and notes</th>
            <th>Example</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Date</td>
            <td>d</td>
            <td>Must be in ISO format: YYYY-MM-DDThh:mm:ss.SSSZ</td>
            <td>{ date: Date(Apr 12, 1961, 09:07 Moscow) } → <br/>date|d24|1961-04-12T06:07:00.000Z;</td>
        </tr>
        <tr>
            <td>String</td>
            <td>l</td>
            <td></td>
            <td>{ name: 'Yuri Gagarin' } → <br/>name|l12|Yuri Gagarin;</td>
        </tr>
        <tr>
            <td>Number</td>
            <td>n</td>
            <td></td>
            <td>{ hrsAtSpace: 1.8 } → <br/>hrsAtSpace|n3|1.8;</td>
        </tr>
        <tr>
            <td>Slip</td>
            <td>s</td>
            <td>
                <b>Slip</b>s can be nested, just like JSON. Pay attention that this <b>Slip</b> becomes a field, so
                there'll be two semicolons at its end: one for the last nested field and one for it itself as a field.
            </td>
            <td>
                { family: { wife: 'Valentina', daughter1: 'Elena', daughter2: 'Galina' } } →
                <br/>family|s57|wife|l9|Valentina;daughter1|l5|Elena;daughter2|l6|Galina;;</td>
        </tr>
        <tr>
            <td>File</td>
            <td>f</td>
            <td>
                When serializing a file, you have to also provide its <i>name</i>. It has to be placed right after the
                content, without any delimiters, before the field-ending semicolon. All the special characters must be
                escaped the same way it's done for the <i>key</i>
            </td>
            <td>{ photo: File('>gagarin.png') } → <br/>photo|f60|fqzcSx6p4ftdQt/K/fR/cr4e+Jfgu40+CO8+z/vJ>>gagarin.png;</td>
        </tr>
        <tr>
            <td>Boolean</td>
            <td>b</td>
            <td>
                Boolean values don't need the <i>size</i> part and the '|' delimiter after it. The <i>content</i> is 1
                when serializing <code>true</code> and 0 for <code>false</code></td>
            <td>{ isFirst: true } → <br/>isFirst|b1;</td>
        </tr>
        <tr>
            <td>Array</td>
            <td>a</td>
            <td>Is serialized as if it was a nested <b>Slip</b>, with indices as keys</td>
            <td>
                { awards: ['Hero of the USSR', 'Order of Lenin', 'Hero of Labor (Vietnam)'] } → 
                <br/>awards|a74|0|l16|Hero of the USSR;1|l14|Order of Lenin;2|l23|Hero of Labor (Vietnam);;
            </td>
        </tr>
        <tr>
            <td>None / Null / Nil / Undefined / etc.</td>
            <td>x</td>
            <td>
                Use this when you want to send something that is not just an empty String, but a 'Nothing'.
                The <i>size</i> and <i>content</i> parts should both be omitted, no delimiters needed as well.
            </td>
            <td>{ googleAccount: null } → <br/>googleAccount|x;</td>
        </tr>
    </tbody>
</table>
