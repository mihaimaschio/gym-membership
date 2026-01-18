const axios = require('axios');

// Configurare
const API_URL = 'http://localhost:8080/api';
let token = '';

// Date pentru login admin (înlocuiește cu datele tale)
const adminCredentials = {
    email: 'admin@gma',  // SCHIMBĂ CU EMAIL-UL TĂU DE ADMIN
    password: 'admin'      // SCHIMBĂ CU PAROLA TA DE ADMIN
};

// Clasele de adăugat
const classes = [
    // LUNI
    { type: 'YOGA', dayOfWeek: 'MONDAY', time: '09:00' },
    { type: 'TRX', dayOfWeek: 'MONDAY', time: '11:00' },
    { type: 'CIRCUIT', dayOfWeek: 'MONDAY', time: '17:00' },
    { type: 'INTERVAL_TRAINING', dayOfWeek: 'MONDAY', time: '19:00' },

    // MARȚI
    { type: 'KICK_BOX', dayOfWeek: 'TUESDAY', time: '09:00' },
    { type: 'YOGA', dayOfWeek: 'TUESDAY', time: '11:00' },
    { type: 'TRX', dayOfWeek: 'TUESDAY', time: '17:00' },
    { type: 'CIRCUIT', dayOfWeek: 'TUESDAY', time: '19:00' },

    // MIERCURI
    { type: 'INTERVAL_TRAINING', dayOfWeek: 'WEDNESDAY', time: '09:00' },
    { type: 'KICK_BOX', dayOfWeek: 'WEDNESDAY', time: '11:00' },
    { type: 'YOGA', dayOfWeek: 'WEDNESDAY', time: '17:00' },
    { type: 'TRX', dayOfWeek: 'WEDNESDAY', time: '19:00' },

    // JOI
    { type: 'CIRCUIT', dayOfWeek: 'THURSDAY', time: '09:00' },
    { type: 'INTERVAL_TRAINING', dayOfWeek: 'THURSDAY', time: '11:00' },
    { type: 'KICK_BOX', dayOfWeek: 'THURSDAY', time: '17:00' },
    { type: 'YOGA', dayOfWeek: 'THURSDAY', time: '19:00' },

    // VINERI
    { type: 'TRX', dayOfWeek: 'FRIDAY', time: '09:00' },
    { type: 'CIRCUIT', dayOfWeek: 'FRIDAY', time: '11:00' },
    { type: 'INTERVAL_TRAINING', dayOfWeek: 'FRIDAY', time: '17:00' },
    { type: 'KICK_BOX', dayOfWeek: 'FRIDAY', time: '19:00' }
];

async function login() {
    try {
        const response = await axios.post(`${API_URL}/auth/login`, adminCredentials);
        token = response.data.token;
        console.log('✅ Login successful!');
        return true;
    } catch (error) {
        console.error('❌ Login failed:', error.response?.data || error.message);
        return false;
    }
}

async function createClass(classData) {
    try {
        const response = await axios.post(
            `${API_URL}/classes?type=${classData.type}&dayOfWeek=${classData.dayOfWeek}&time=${classData.time}`,
            {},
            {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }
        );
        console.log(`✅ Created: ${classData.type} on ${classData.dayOfWeek} at ${classData.time}`);
        return true;
    } catch (error) {
        console.error(`❌ Failed to create ${classData.type}:`, error.response?.data || error.message);
        return false;
    }
}

async function addAllClasses() {
    console.log('🏋️ Starting to add gym classes...\n');

    // Login first
    const loggedIn = await login();
    if (!loggedIn) {
        console.log('\n❌ Cannot proceed without login. Please check your credentials.');
        return;
    }

    console.log('\n📝 Adding classes...\n');

    // Add all classes
    let successCount = 0;
    for (const classData of classes) {
        const success = await createClass(classData);
        if (success) successCount++;
        // Small delay to avoid overwhelming the server
        await new Promise(resolve => setTimeout(resolve, 100));
    }

    console.log(`\n✅ Done! Successfully added ${successCount}/${classes.length} classes.`);
}

// Run the script
addAllClasses();
