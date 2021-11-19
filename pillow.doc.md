# Pillow
**Pillow** is the request / response format <u>Toss-a-Message</u> supports

## 1. Request

<table>
    <thead>
        <caption><b>Request payload fields</b></caption>
        <tr>
            <th>Field</th>
            <th>Is Required</th>
            <th>Data Type</th>
            <th>Restrictions</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>action</td>
            <td>✓</td>
            <td>String</td>
            <td>Must be one of ['send-message', 'log-in', 'chunks']</td>
        </tr>
        <tr>
            <td>data</td>
            <td></td>
            <td>Object</td>
            <td>Check the <b>'data' fields</b> table below</td>
        </tr>
    </tbody>
</table>

<table>
    <thead>
        <caption><b>'data' fields</b></caption>
        <tr>
            <th>Field</th>
            <th>Used for actions</th>
            <th>Data Type</th>
            <th>Restrictions</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>username</td>
            <td>'log-in'</td>
            <td>String</td>
            <td></td>
        </tr>
        <tr>
            <td>message</td>
            <td>'send-message'</td>
            <td>String</td>
            <td></td>
        </tr>
        <tr>
            <td>attachment</td>
            <td>'send-message'</td>
            <td>Object</td>
            <td>Must have exactly two subfields: 'file' (a Byte Array) and 'name' (a String)</td>
        </tr>
        <tr>
            <td>chunks</td>
            <td>'chunks'</td>
            <td>Number</td>
            <td></td>
        </tr>
    </tbody>
</table>

When sending a message, at least one of 'message' and 'attachment' must be provided.

## 2. Response

<table>
    <thead>
        <caption><b>Response payload fields</b></caption>
        <tr>
            <th>Field</th>
            <th>Is present</th>
            <th>Data Type</th>
            <th>Restrictions</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>action</td>
            <td>
                UNLESS (<br/>
                &nbsp;&nbsp;there's a server error not caused by client request OR<br/>
                &nbsp;&nbsp;the action could not be extracted from the request<br/>
                )
            </td>
            <td>String</td>
            <td>One of ['send-message', 'log-in', 'log-out', 'close-server']</td>
        </tr>
        <tr>
            <td>status</td>
            <td>always</td>
            <td>Number</td>
            <td>Check the <b>Response status</b> table below</td>
        </tr>
        <tr>
            <td>data</td>
            <td>if 'status' is not 101</td>
            <td>Object</td>
            <td>Check the <b>'data' fields</b> table below</td>
        </tr>
    </tbody>
</table>

<table>
    <thead>
        <caption><b>Response status</b></caption>
        <tr>
            <th>Code value</th>
            <th>Meaning</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>100</td>
            <td>Client data received and processed, no errors occurred, some data sent back</td>
        </tr>
        <tr>
            <td>101</td>
            <td>Client data received and processed, no errors occurred, but no data to send back</td>
        </tr>
        <tr>
            <td>200</td>
            <td>The data provided in the request is invalid</td>
        </tr>
        <tr>
            <td>201</td>
            <td>The data provided in the request is not properly serialized, could not parse it</td>
        </tr>
        <tr>
            <td>202</td>
            <td>Server error</td>
        </tr>
    </tbody>
</table>

Overall, the statuses with codes above 199 represent that some error occurred

<table>
    <thead>
        <caption><b>'data' fields</b></caption>
        <tr>
            <th>Field</th>
            <th>Is present when</th>
            <th>Data Type</th>
            <th>Restrictions</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>time</td>
            <td>status < 200</td>
            <td>Date</td>
            <td></td>
        </tr>
        <tr>
            <td>message</td>
            <td>
                (status < 200) AND<br/>
                (action == 'send-message')
            </td>
            <td>String</td>
            <td>May be Null</td>
        </tr>
        <tr>
            <td>attachment</td>
            <td>
                (status < 200) AND <br/>
                (action == 'send-message')
            </td>
            <td>Object</td>
            <td>May be Null. If not Null, has two subfields: 'file' (a Byte Array) and 'name' (a String)</td>
        </tr>
        <tr>
            <td>username</td>
            <td>
                status < 200;<br/>
                for 'log-in' and 'log-out', - not sent to the client who's logging in / out
            </td>
            <td>String</td>
            <td></td>
        </tr>
        <tr>
            <td>errors</td>
            <td>status >= 200</td>
            <td>Object</td>
            <td>Each key is the name of the field that's invalid, each value is an array of errors</td>
        </tr>
        <tr>
            <td>chunks</td>
            <td>action == 'chunks'</td>
            <td>Number</td>
            <td></td>
        </tr>
    </tbody>
</table>
Note that if status is 101, there will be no data at all.