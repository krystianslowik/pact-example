const path = require('path');
const { Pact, Matchers } = require('@pact-foundation/pact');
const axios = require('axios');
const { like } = Matchers;
const { expect } = require('chai');

describe('Pact with pact-example', () => {
    // Configure the Pact mock provider
    const provider = new Pact({
        consumer: 'pact-consumer-example',
        provider: 'pact-example',
        port: 1234,
        log: path.resolve(process.cwd(), 'logs', 'pact.log'),
        dir: path.resolve(process.cwd(), 'pacts'),
        logLevel: 'INFO'
    });

    // Setup the mock server before the tests run
    before(async () => {
        await provider.setup();
    });

    // Verify interactions and shutdown the mock server after tests
    after(async () => {
        await provider.finalize();
    });

    afterEach(async () => {
        await provider.verify();
    });

    describe('GET /projects/123', () => {
        before(async () => {
            // Define the expected interaction
            await provider.addInteraction({
                state: 'project with ID 123 exists',
                uponReceiving: 'a request to get project 123',
                withRequest: {
                    method: 'GET',
                    path: '/projects/123'
                },
                willRespondWith: {
                    status: 200,
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: {
                        id: like('123'),
                        name: like('Agile Transformation'),
                        status: like('Active')
                    }
                }
            });
        });

        it('returns the project details', async () => {
            // Call the mock provider endpoint
            const response = await axios.get('http://localhost:1234/projects/123');
            expect(response.status).to.equal(200);
            expect(response.data).to.deep.equal({
                id: '123',
                name: 'Agile Transformation',
                status: 'Active'
            });
        });
    });
});