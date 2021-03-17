const { getByPK, fetchBySK, insert, update } = require('./query');
const { removeConnectionIds } = require('./connection');

const ProjectionExpression =
    'username, #name, email, avatar, avatarKey, avatarExpiration, #status, #role, phone, companyId, customName, locationUpdate, color, locationEnabled, notificationEnabled, pushToTalkEnabled';

async function getUser(username, companyId) {
    return getByPK({
        ExpressionAttributeValues: {
            ':pk': `USER#${username}`,
            ':sk': `CONFIG#${companyId || ''}`,
        },
        ExpressionAttributeNames: {
            '#status': 'status',
            '#role': 'role',
            '#name': 'name',
        },
        ProjectionExpression,
    });
}

async function fetchUsers(companyId) {
    return fetchBySK({
        ExpressionAttributeValues: {
            ':sk': `CONFIG#${companyId}`,
            ':pk': 'USER#',
        },
        ExpressionAttributeNames: {
            '#status': 'status',
            '#role': 'role',
            '#name': 'name',
        },
        ProjectionExpression,
    });
}

async function createUser(data, companyId) {
    const { password, ...user } = data;
    await insert({
        ...user,
        locationUpdate: 30,
        partitionKey: `USER#${data.email}`,
        username: data.email,
        sortKey: `CONFIG#${companyId}`,
        companyId,
    });
}

async function updateUser(
    {
        customName = '',
        name,
        phone,
        email,
        role,
        status,
        avatar,
        avatarKey,
        avatarExpiration,
        locationEnabled,
        pushToTalkEnabled,
        notificationEnabled,
    },
    username,
    companyId,
) {
    return update({
        Key: {
            partitionKey: `USER#${username}`,
            sortKey: `CONFIG#${companyId}`,
        },
        UpdateExpression:
            'set customName = :customName, phone = :phone, email = :email, #role = :role, #status = :status, #name = :name, #avatar = :avatar, #avatarKey = :avatarKey, #avatarExpiration = :avatarExpiration,#locationEnabled = :locationEnabled, #pushToTalkEnabled = :pushToTalkEnabled, #notificationEnabled = :notificationEnabled',
        ExpressionAttributeNames: {
            '#status': 'status',
            '#role': 'role',
            '#name': 'name',
            '#avatar': 'avatar',
            '#avatarExpiration': 'avatarExpiration',
            '#locationEnabled': 'locationEnabled',
            '#pushToTalkEnabled': 'pushToTalkEnabled',
            '#notificationEnabled': 'notificationEnabled',
            '#avatarKey': 'avatarKey',
        },
        ExpressionAttributeValues: {
            ':customName': customName,
            ':phone': phone,
            ':email': email,
            ':role': role,
            ':status': status,
            ':name': name,
            ':avatar': avatar,
            ':avatarExpiration': avatarExpiration,
            ':locationEnabled': locationEnabled,
            ':pushToTalkEnabled': pushToTalkEnabled,
            ':notificationEnabled': notificationEnabled,
            ':avatarKey': avatarKey,
        },
    });
}

async function disableUser(username, companyId) {
    await removeConnectionIds(username);
    return update({
        Key: {
            partitionKey: `USER#${username}`,
            sortKey: `CONFIG#${companyId}`,
        },
        UpdateExpression: 'set #status = :status',
        ExpressionAttributeNames: {
            '#status': 'status',
        },
        ExpressionAttributeValues: {
            ':status': 'DISABLED',
        },
    });
}

module.exports = {
    getUser,
    fetchUsers,
    createUser,
    updateUser,
    disableUser,
};
