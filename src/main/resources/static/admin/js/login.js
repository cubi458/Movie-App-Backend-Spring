async function handleLogin(event) {
    event.preventDefault();
    
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    
    console.log('Attempting login for email:', email);

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();
        console.log('Login response:', data);

        if (response.ok) {
            console.log('Login successful, storing tokens...');
            localStorage.setItem('accessToken', data.accessToken);
            localStorage.setItem('refreshToken', data.refreshToken);
            localStorage.setItem('userEmail', data.email);
            localStorage.setItem('userName', data.name);
            localStorage.setItem('userRole', data.role);
            
            console.log('Stored values:', {
                accessToken: localStorage.getItem('accessToken'),
                userRole: localStorage.getItem('userRole'),
                userEmail: localStorage.getItem('userEmail')
            });

            if (data.role === 'ROLE_ADMIN') {
                console.log('Admin role detected, redirecting to dashboard...');
                window.location.href = '/admin/dashboard.html';
            } else {
                console.error('Access denied: Admin role required');
                alert('Access denied: Admin role required');
            }
        } else {
            console.error('Login failed:', data);
            alert('Login failed: ' + (data.message || 'Unknown error'));
        }
    } catch (error) {
        console.error('Login error:', error);
        alert('Login failed: ' + error.message);
    }
} 