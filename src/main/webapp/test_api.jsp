<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<script>
    async function sendRequest(authHeader = null) {
        const url = 'auth';  // шлях до вашого API
        const options = {
            method: 'GET',
            headers: {}
        };
        if (authHeader) {
            options.headers['Authorization'] = authHeader;
        }

        try {
            const response = await fetch(url, options);
            const data = await response.json();
            displayResponse(response.status, data);
        } catch (error) {
            displayResponse('Error', error);
        }
    }

    function displayResponse(status, data) {
        const resultDiv = document.getElementById('result');
        resultDiv.innerHTML += `<p>Status: ${status}, Response: ${JSON.stringify(data)}</p>`;
    }

    function testWithoutAuth() {
        sendRequest();
    }

    function testInvalidScheme() {
        sendRequest('Bearer someInvalidToken');
    }

    function testCorrectAuth() {
        const login = '234';
        const password = '123';
        const credentials = btoa(`${login}:${password}`);
        sendRequest(`Basic ${credentials}`);
    }
</script>
<h1>API Testing Page</h1>
<button onclick="testWithoutAuth()">Test without Authorization Header</button>
<button onclick="testInvalidScheme()">Test with Invalid Authorization Scheme</button>
<button onclick="testCorrectAuth()">Test with Correct Credentials</button>
<div id="result"></div>
