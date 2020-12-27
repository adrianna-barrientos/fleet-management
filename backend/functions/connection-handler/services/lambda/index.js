const Lambda = require('aws-sdk/clients/lambda');

const lambda = new Lambda();

const sendMessage = async (user, connectionIds, action) => {
    return lambda
        .invoke({
            FunctionName: `${process.env.APP}-${process.env.STAGE}-ws-post-message`,
            Payload: JSON.stringify({
                connectionIds,
                data: {
                    action,
                    body: user,
                },
            }),
            InvocationType: 'RequestResponse',
        })
        .promise();
};

module.exports = {
    sendMessage,
};