document.addEventListener('DOMContentLoaded', async function() {
    const token = localStorage.getItem('accessToken');
    const userRole = localStorage.getItem('userRole');
    
    console.log('Token from localStorage:', token);
    console.log('User role from localStorage:', userRole);
    
    if (!token || !userRole) {
        console.error('No authentication credentials found');
        window.location.href = '/admin/login.html';
        return;
    }

    try {
        // First verify authentication
        const authResponse = await fetch('/api/auth/check', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!authResponse.ok) {
            throw new Error('Authentication failed');
        }

        // Add dark theme styles
        const style = document.createElement('style');
        style.textContent = `
            :root {
                --netflix-red: #E50914;
                --netflix-dark: #141414;
                --netflix-darker: #0B0B0B;
                --netflix-gray: #808080;
                --netflix-light: #E5E5E5;
            }

            body {
                background-color: var(--netflix-dark);
                color: var(--netflix-light);
                font-family: 'Netflix Sans', 'Helvetica Neue', Helvetica, Arial, sans-serif;
            }

            .sidebar {
                background-color: var(--netflix-darker);
                border-right: 1px solid rgba(255,255,255,0.1);
                padding: 20px;
                height: 100vh;
                position: fixed;
                width: 250px;
                transition: all 0.3s ease;
            }

            .sidebar:hover {
                box-shadow: 0 0 20px rgba(0,0,0,0.5);
            }

            .main-content {
                margin-left: 250px;
                padding: 30px;
                min-height: 100vh;
                background: linear-gradient(to bottom, var(--netflix-darker) 0%, var(--netflix-dark) 100%);
            }

            .nav-link {
                color: var(--netflix-gray) !important;
                transition: all 0.3s ease;
                padding: 12px 15px;
                border-radius: 8px;
                margin-bottom: 5px;
                position: relative;
                overflow: hidden;
            }

            .nav-link:before {
                content: '';
                position: absolute;
                left: 0;
                bottom: 0;
                width: 0;
                height: 2px;
                background-color: var(--netflix-red);
                transition: width 0.3s ease;
            }

            .nav-link:hover:before, .nav-link.active:before {
                width: 100%;
            }

            .nav-link:hover, .nav-link.active {
                background-color: rgba(255,255,255,0.1);
                color: var(--netflix-light) !important;
                transform: translateX(5px);
            }

            .nav-link i {
                margin-right: 10px;
                transition: transform 0.3s ease;
            }

            .nav-link:hover i {
                transform: scale(1.2);
            }

            .movie-form {
                background-color: rgba(0,0,0,0.6);
                padding: 30px;
                border-radius: 15px;
                margin-bottom: 30px;
                backdrop-filter: blur(10px);
                border: 1px solid rgba(255,255,255,0.1);
                box-shadow: 0 10px 30px rgba(0,0,0,0.3);
                transition: transform 0.3s ease, box-shadow 0.3s ease;
            }

            .movie-form:hover {
                transform: translateY(-5px);
                box-shadow: 0 15px 40px rgba(0,0,0,0.4);
            }

            .form-control, .form-select {
                background-color: rgba(255,255,255,0.1);
                border: 1px solid rgba(255,255,255,0.2);
                color: var(--netflix-light);
                transition: all 0.3s ease;
            }

            .form-control:focus, .form-select:focus {
                background-color: rgba(255,255,255,0.15);
                border-color: var(--netflix-red);
                color: var(--netflix-light);
                box-shadow: 0 0 0 2px rgba(229,9,20,0.25);
            }

            .btn-primary {
                background-color: var(--netflix-red);
                border: none;
                transition: all 0.3s ease;
                position: relative;
                overflow: hidden;
            }

            .btn-primary:hover {
                background-color: #f40612;
                transform: translateY(-2px);
                box-shadow: 0 5px 15px rgba(229,9,20,0.4);
            }

            .btn-primary:active {
                transform: translateY(0);
            }

            .table {
                color: var(--netflix-light);
                background-color: transparent;
                border-radius: 15px;
                overflow: hidden;
                backdrop-filter: blur(10px);
                margin: 0;
            }

            .table thead th {
                background-color: rgba(0,0,0,0.6);
                border-bottom: 2px solid var(--netflix-red);
                color: var(--netflix-light);
                font-weight: 500;
                text-transform: uppercase;
                letter-spacing: 1px;
                padding: 15px;
            }

            .table tbody tr {
                transition: all 0.3s ease;
                background-color: rgba(20, 20, 20, 0.6);
                border-bottom: 1px solid rgba(255,255,255,0.05);
            }

            .table tbody tr:hover {
                background-color: rgba(40, 40, 40, 0.9);
                transform: scale(1.01);
                box-shadow: 0 5px 15px rgba(0,0,0,0.3);
            }

            .table td {
                padding: 15px;
                vertical-align: middle;
                border-color: rgba(255,255,255,0.05);
            }

            .movie-list {
                background-color: rgba(0,0,0,0.4);
                border-radius: 15px;
                padding: 25px;
                box-shadow: 0 10px 30px rgba(0,0,0,0.2);
                backdrop-filter: blur(10px);
                border: 1px solid rgba(255,255,255,0.05);
            }

            .movie-list h4 {
                color: var(--netflix-light);
                margin-bottom: 20px;
                font-weight: 500;
                letter-spacing: 0.5px;
            }

            .table-responsive {
                border-radius: 10px;
                overflow: hidden;
            }

            /* Styling for action buttons in table */
            .btn-action {
                padding: 8px 12px;
                border-radius: 6px;
                transition: all 0.3s ease;
                margin: 0 3px;
                background-color: rgba(255,255,255,0.1);
                border: none;
                backdrop-filter: blur(5px);
            }

            .btn-action:hover {
                transform: translateY(-2px);
                box-shadow: 0 5px 15px rgba(0,0,0,0.3);
            }

            .btn-action.btn-info {
                background-color: rgba(23, 162, 184, 0.2);
                color: #17a2b8;
            }

            .btn-action.btn-danger {
                background-color: rgba(220, 53, 69, 0.2);
                color: #dc3545;
            }

            .btn-action.btn-primary {
                background-color: rgba(0, 123, 255, 0.2);
                color: #007bff;
            }

            /* Styling for movie title */
            .movie-title {
                font-weight: 500;
                color: var(--netflix-light);
                transition: all 0.3s ease;
            }

            .movie-title:hover {
                color: var(--netflix-red);
            }

            /* Styling for trailer link */
            .trailer-link {
                color: var(--netflix-gray);
                text-decoration: none;
                transition: all 0.3s ease;
                display: inline-block;
                max-width: 200px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }

            .trailer-link:hover {
                color: var(--netflix-red);
            }

            .movie-poster-container {
                transition: all 0.3s ease;
                border-radius: 8px;
                overflow: hidden;
                position: relative;
            }

            .movie-poster-container:hover {
                transform: scale(1.1);
                box-shadow: 0 10px 20px rgba(0,0,0,0.4);
                z-index: 1;
            }

            .movie-poster-container img {
                transition: all 0.3s ease;
            }

            .movie-poster-container:hover img {
                filter: brightness(1.2);
            }

            .alert {
                border-radius: 8px;
                border: none;
                animation: slideIn 0.5s ease;
                backdrop-filter: blur(10px);
            }

            @keyframes slideIn {
                from {
                    transform: translateY(-100%);
                    opacity: 0;
                }
                to {
                    transform: translateY(0);
                    opacity: 1;
                }
            }

            .modal-content {
                background-color: var(--netflix-darker);
                color: var(--netflix-light);
                border: none;
                border-radius: 15px;
                backdrop-filter: blur(20px);
                box-shadow: 0 15px 40px rgba(0,0,0,0.5);
            }

            .modal-header {
                border-bottom: 1px solid rgba(255,255,255,0.1);
                padding: 20px;
            }

            .modal-body {
                padding: 30px;
            }

            .btn-close {
                filter: invert(1) grayscale(100%) brightness(200%);
                transition: transform 0.3s ease;
            }

            .btn-close:hover {
                transform: rotate(90deg);
            }

            .video-wrapper {
                background-color: #000;
                border-radius: 15px;
                overflow: hidden;
                box-shadow: 0 10px 30px rgba(0,0,0,0.5);
                transition: transform 0.3s ease;
            }

            .video-wrapper:hover {
                transform: scale(1.02);
            }

            .subtitle-controls {
                background-color: rgba(0,0,0,0.6);
                padding: 20px;
                border-radius: 12px;
                margin-top: 20px;
                backdrop-filter: blur(10px);
                border: 1px solid rgba(255,255,255,0.1);
                transition: all 0.3s ease;
            }

            .subtitle-controls:hover {
                background-color: rgba(0,0,0,0.8);
            }

            /* Custom scrollbar */
            ::-webkit-scrollbar {
                width: 8px;
                height: 8px;
            }

            ::-webkit-scrollbar-track {
                background: var(--netflix-darker);
            }

            ::-webkit-scrollbar-thumb {
                background: var(--netflix-red);
                border-radius: 4px;
            }

            ::-webkit-scrollbar-thumb:hover {
                background: #f40612;
            }

            /* Loading animation */
            .loading-animation {
                width: 40px;
                height: 40px;
                border: 3px solid rgba(255,255,255,0.3);
                border-radius: 50%;
                border-top-color: var(--netflix-red);
                animation: spin 1s ease-in-out infinite;
            }

            @keyframes spin {
                to { transform: rotate(360deg); }
            }

            /* Button animations */
            .btn {
                position: relative;
                overflow: hidden;
            }

            .btn::after {
                content: '';
                position: absolute;
                top: 50%;
                left: 50%;
                width: 0;
                height: 0;
                background: rgba(255,255,255,0.2);
                border-radius: 50%;
                transform: translate(-50%, -50%);
                transition: width 0.6s ease, height 0.6s ease;
            }

            .btn:active::after {
                width: 300px;
                height: 300px;
                opacity: 0;
            }

            /* Fade in animation for content */
            .fade-in {
                animation: fadeIn 0.5s ease;
            }

            @keyframes fadeIn {
                from { opacity: 0; transform: translateY(20px); }
                to { opacity: 1; transform: translateY(0); }
            }
        `;
        document.head.appendChild(style);

        // If authentication successful, load dashboard content
        document.getElementById('loading').style.display = 'none';
        const dashboardContent = document.getElementById('dashboard-content');
        dashboardContent.style.display = 'block';
        dashboardContent.className = 'fade-in';
        
        // Load dashboard HTML content
        dashboardContent.innerHTML = `
            <nav class="sidebar">
                <div class="sidebar-sticky">
                    <div class="text-center mb-4">
                        <h3 class="text-white mb-0">Movie Admin</h3>
                        <small class="text-muted">Management Dashboard</small>
                    </div>
                    <ul class="nav flex-column">
                        <li class="nav-item">
                            <a class="nav-link active" href="#" id="moviesLink">
                                <i class="fas fa-film"></i>
                                Movies
                            </a>
                        </li>
                        <li class="nav-item mt-auto">
                            <a class="nav-link" href="#" id="logoutBtn">
                                <i class="fas fa-sign-out-alt"></i>
                                Logout
                            </a>
                        </li>
                    </ul>
                </div>
            </nav>

            <main class="main-content">
                <div class="container-fluid">
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <h2 class="mb-0 fade-in">Movie Management</h2>
                        <div class="d-flex">
                            <button class="btn btn-outline-light ms-2" onclick="loadMovies()">
                                <i class="fas fa-sync-alt me-2"></i>Refresh
                            </button>
                        </div>
                    </div>
                    
                    <div id="alertContainer"></div>
                    
                    <div class="movie-form fade-in">
                        <h4 class="mb-4">Add New Movie</h4>
                        <form id="movieForm" class="mb-4">
                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label for="movieTitle" class="form-label">Title</label>
                                    <input type="text" class="form-control" id="movieTitle" required>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label for="movieDirector" class="form-label">Director</label>
                                    <input type="text" class="form-control" id="movieDirector">
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label for="movieStudio" class="form-label">Studio</label>
                                    <input type="text" class="form-control" id="movieStudio">
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label for="movieYear" class="form-label">Release Year</label>
                                    <input type="number" class="form-control" id="movieYear" required>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-12 mb-3">
                                    <label for="moviePoster" class="form-label">Poster Image</label>
                                    <input type="file" class="form-control" id="moviePoster" accept="image/*" required>
                                    <div id="posterPreview" class="mt-2" style="max-width: 200px; display: none;">
                                        <img src="" alt="Poster Preview" class="img-fluid rounded">
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-12 mb-3">
                                    <label for="movieVideo" class="form-label">Movie Video</label>
                                    <input type="file" class="form-control" id="movieVideo" accept="video/*">
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-12 mb-3">
                                    <label for="movieTrailer" class="form-label">Trailer Link</label>
                                    <input type="url" class="form-control" id="movieTrailer" placeholder="https://example.com/trailer.mp4">
                                </div>
                            </div>
                            <button type="submit" class="btn btn-primary">
                                <i class="fas fa-plus me-2"></i>Add Movie
                            </button>
                        </form>
                    </div>

                    <div class="movie-list fade-in">
                        <h4 class="mb-4">Movie List</h4>
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Poster</th>
                                        <th>Title</th>
                                        <th>Director</th>
                                        <th>Studio</th>
                                        <th>Release Year</th>
                                        <th>Trailer Link</th>
                                        <th>Video</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody id="movieTableBody">
                                    <!-- Movies will be loaded here -->
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </main>

            <!-- Video Preview Modal -->
            <div class="modal fade" id="videoPreviewModal" tabindex="-1">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">Video Preview</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <div id="videoContainer">
                                <!-- Video player will be added here -->
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Poster Preview Modal -->
            <div class="modal fade" id="posterPreviewModal" tabindex="-1">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">Poster Preview</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body text-center">
                            <img id="posterPreviewImage" src="" alt="Movie Poster" class="img-fluid rounded">
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Add event listeners and load data
        setupEventListeners();
        await loadMovies();

    } catch (error) {
        console.error('Authentication failed:', error);
        localStorage.clear();
        window.location.href = '/admin/login.html';
    }
});

function showAlert(message, type = 'success') {
    const alertContainer = document.getElementById('alertContainer');
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible fade show`;
    alert.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    alertContainer.appendChild(alert);
    
    // Auto dismiss after 5 seconds
    setTimeout(() => {
        alert.remove();
    }, 5000);
}

function setupEventListeners() {
    // Logout button
    document.getElementById('logoutBtn').addEventListener('click', () => {
        localStorage.clear();
        window.location.href = '/admin/login.html';
    });

    // Movie form
    document.getElementById('movieForm').addEventListener('submit', handleMovieSubmit);
    
    // Movies link
    document.getElementById('moviesLink').addEventListener('click', (e) => {
        e.preventDefault();
        loadMovies();
    });

    // Add poster preview for new movie
    document.getElementById('moviePoster').addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            const preview = document.getElementById('posterPreview');
            const previewImg = preview.querySelector('img');
            
            reader.onload = function(e) {
                previewImg.src = e.target.result;
                preview.style.display = 'block';
            }
            
            reader.readAsDataURL(file);
        }
    });
}

async function loadMovies() {
    try {
        const token = localStorage.getItem('accessToken');
        const response = await fetch('/api/v1/movie/all', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                throw new Error('Authentication failed');
            }
            throw new Error('Failed to load movies');
        }

        const movies = await response.json();
        const tableBody = document.getElementById('movieTableBody');
        tableBody.innerHTML = '';
        
        movies.forEach(movie => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${movie.id}</td>
                <td>
                    ${movie.posterUrl ? 
                        `<div class="movie-poster-container" style="width: 100px; cursor: pointer;" onclick="previewPoster('${movie.posterUrl}', '${movie.title}')">
                            <img src="${movie.posterUrl}" alt="${movie.title}" class="img-fluid rounded" onerror="this.onerror=null; this.src='/admin/images/no-poster.png';">
                        </div>` : 
                        `<div class="movie-poster-container bg-dark d-flex align-items-center justify-content-center" style="width: 100px; height: 150px;">
                            <i class="fas fa-image text-muted"></i>
                        </div>`
                    }
                </td>
                <td><span class="movie-title">${movie.title}</span></td>
                <td>${movie.director || 'N/A'}</td>
                <td>${movie.studio || 'N/A'}</td>
                <td>${movie.releaseYear}</td>
                <td>
                    ${movie.trailerLink ? 
                        `<div class="d-flex align-items-center">
                            <a href="${movie.trailerLink}" target="_blank" class="trailer-link me-2" title="${movie.trailerLink}">
                                ${movie.trailerLink}
                            </a>
                            <button class="btn btn-action btn-info" onclick="previewVideo('${movie.trailerLink}', '${movie.title}', true)">
                                <i class="fas fa-film"></i>
                            </button>
                        </div>` : 
                        'N/A'
                    }
                </td>
                <td>
                    ${movie.video ? 
                        `<button class="btn btn-action btn-primary" onclick="previewVideo('${movie.videoUrl}', '${movie.title}')">
                            <i class="fas fa-play"></i>
                        </button>` : 
                        '<span class="badge bg-secondary">No Video</span>'
                    }
                </td>
                <td>
                    <button class="btn btn-action btn-danger" onclick="deleteMovie(${movie.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            `;
            tableBody.appendChild(row);
        });
    } catch (error) {
        console.error('Error loading movies:', error);
        if (error.message === 'Authentication failed') {
            localStorage.clear();
            window.location.href = '/admin/login.html';
        } else {
            showAlert('Failed to load movies', 'danger');
        }
    }
}

async function handleMovieSubmit(e) {
    e.preventDefault();
    const token = localStorage.getItem('accessToken');
    
    try {
        const formData = new FormData();
        const posterFile = document.getElementById('moviePoster').files[0];
        const videoFile = document.getElementById('movieVideo').files[0];
        const trailerLink = document.getElementById('movieTrailer').value;

        if (!posterFile) {
            showAlert('Please select a poster image', 'danger');
            return;
        }

        const movieData = {
            title: document.getElementById('movieTitle').value,
            director: document.getElementById('movieDirector').value || 'N/A',
            studio: document.getElementById('movieStudio').value || 'N/A', 
            releaseYear: parseInt(document.getElementById('movieYear').value),
            trailerLink: trailerLink || null,
            video: videoFile ? true : false
        };

        formData.append('file', posterFile);
        if (videoFile) {
            formData.append('video', videoFile);
        }
        formData.append('movieDto', JSON.stringify(movieData));

        const response = await fetch('/api/v1/movie/add-movie', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });

        if (!response.ok) {
            throw new Error('Failed to add movie');
        }

        showAlert('Movie added successfully!');
        document.getElementById('movieForm').reset();
        await loadMovies();
        
    } catch (error) {
        console.error('Error adding movie:', error);
        if (error.message === 'Authentication failed') {
            localStorage.clear();
            window.location.href = '/admin/login.html';
        } else {
            showAlert('Failed to add movie', 'danger');
        }
    }
}

async function deleteMovie(id) {
    const token = localStorage.getItem('accessToken');
    if (confirm('Are you sure you want to delete this movie?')) {
        try {
            const response = await fetch(`/api/v1/movie/delete/${id}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error('Failed to delete movie');
            }

            showAlert('Movie deleted successfully!');
            await loadMovies();
        } catch (error) {
            console.error('Error deleting movie:', error);
            if (error.message === 'Authentication failed') {
                localStorage.clear();
                window.location.href = '/admin/login.html';
            } else {
                showAlert('Failed to delete movie', 'danger');
            }
        }
    }
}

// Add video preview function
function previewVideo(videoUrl, title, isTrailer = false) {
    const modal = new bootstrap.Modal(document.getElementById('videoPreviewModal'));
    const videoContainer = document.getElementById('videoContainer');
    document.querySelector('#videoPreviewModal .modal-title').textContent = `Preview: ${title}`;

    // Xóa nội dung cũ
    videoContainer.innerHTML = '';

    if (isTrailer && videoUrl.includes('youtube.com/embed/')) {
        // Nếu là YouTube trailer
        const iframe = document.createElement('iframe');
        iframe.src = videoUrl + '?cc_load_policy=1';
        iframe.width = '100%';
        iframe.height = '480';
        iframe.allowFullscreen = true;
        iframe.allow = 'accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture';
        videoContainer.appendChild(iframe);
    } else {
        // Nếu là video file
        const videoWrapper = document.createElement('div');
        videoWrapper.className = 'video-wrapper';
        
        const video = document.createElement('video');
        video.src = videoUrl;
        video.className = 'w-100';
        video.controls = true;
        video.crossOrigin = 'anonymous';

        // Thêm thư viện subtitle.js để hỗ trợ .ass
        const scriptAss = document.createElement('script');
        scriptAss.src = 'https://cdn.jsdelivr.net/npm/ass-parser@0.4.0/dist/ass-parser.min.js';
        document.head.appendChild(scriptAss);

        // Thêm controls cho phụ đề
        const subtitleControls = document.createElement('div');
        subtitleControls.className = 'subtitle-controls mt-2';
        subtitleControls.innerHTML = `
            <div class="d-flex align-items-center mb-2">
                <label class="me-2">Phụ đề:</label>
                <select class="form-select form-select-sm me-2" style="width: auto;" id="subtitleSelect">
                    <option value="off">Tắt</option>
                </select>
                <input type="file" class="form-control form-control-sm" id="subtitleFile" 
                       accept=".srt,.vtt,.ass,.ssa,.sub" style="width: auto;">
            </div>
        `;

        // Xử lý khi video load metadata
        video.addEventListener('loadedmetadata', function() {
            const subtitleSelect = subtitleControls.querySelector('#subtitleSelect');
            
            // Xử lý các track phụ đề có sẵn
            if (video.textTracks && video.textTracks.length > 0) {
                for (let i = 0; i < video.textTracks.length; i++) {
                    const track = video.textTracks[i];
                    if (track.kind === 'subtitles' || track.kind === 'captions') {
                        const option = document.createElement('option');
                        option.value = i;
                        option.textContent = track.label || `Phụ đề ${i + 1}`;
                        subtitleSelect.appendChild(option);
                    }
                }
            }

            // Xử lý khi thay đổi phụ đề
            subtitleSelect.addEventListener('change', function() {
                const value = this.value;
                if (value === 'off') {
                    for (let i = 0; i < video.textTracks.length; i++) {
                        video.textTracks[i].mode = 'hidden';
                    }
                } else {
                    for (let i = 0; i < video.textTracks.length; i++) {
                        video.textTracks[i].mode = (i === parseInt(value)) ? 'showing' : 'hidden';
                    }
                }
            });
        });

        // Xử lý khi upload file phụ đề
        const subtitleFile = subtitleControls.querySelector('#subtitleFile');
        subtitleFile.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                const fileExtension = file.name.split('.').pop().toLowerCase();
                const reader = new FileReader();

                reader.onload = function(e) {
                    const subtitleContent = e.target.result;
                    let track;

                    // Xóa track cũ nếu có
                    const oldTrack = video.querySelector('track');
                    if (oldTrack) oldTrack.remove();

                    // Tạo track mới
                    track = document.createElement('track');
                    track.kind = 'subtitles';
                    track.label = file.name;
                    track.default = true;

                    if (fileExtension === 'ass' || fileExtension === 'ssa') {
                        // Xử lý file .ass/.ssa
                        try {
                            const parser = new ASS();
                            const parsedSubs = parser.parse(subtitleContent);
                            // Chuyển đổi sang VTT
                            const vttContent = convertAssToVTT(parsedSubs);
                            const vttBlob = new Blob([vttContent], { type: 'text/vtt' });
                            track.src = URL.createObjectURL(vttBlob);
                        } catch (error) {
                            console.error('Error parsing ASS subtitle:', error);
                            alert('Không thể đọc file phụ đề này. Vui lòng thử file khác.');
                            return;
                        }
                    } else {
                        // Xử lý các định dạng khác (srt, vtt, sub)
                        const blob = new Blob([subtitleContent], { type: 'text/vtt' });
                        track.src = URL.createObjectURL(blob);
                    }

                    video.appendChild(track);
                    
                    // Thêm option mới vào select
                    const subtitleSelect = document.getElementById('subtitleSelect');
                    const option = document.createElement('option');
                    option.value = video.textTracks.length - 1;
                    option.textContent = file.name;
                    subtitleSelect.appendChild(option);
                    option.selected = true;

                    // Kích hoạt track
                    video.textTracks[video.textTracks.length - 1].mode = 'showing';
                };

                if (fileExtension === 'ass' || fileExtension === 'ssa') {
                    reader.readAsText(file);
                } else {
                    reader.readAsText(file);
                }
            }
        });

        videoWrapper.appendChild(video);
        videoWrapper.appendChild(subtitleControls);
        videoContainer.appendChild(videoWrapper);
    }

    modal.show();
    
    // Cleanup khi đóng modal
    document.getElementById('videoPreviewModal').addEventListener('hidden.bs.modal', function () {
        videoContainer.innerHTML = '';
        const scriptAss = document.querySelector('script[src*="ass-parser"]');
        if (scriptAss) scriptAss.remove();
    });
}

// Hàm chuyển đổi ASS sang VTT
function convertAssToVTT(assData) {
    let vttContent = 'WEBVTT\n\n';
    
    // Chuyển đổi từng dialogue trong ASS sang định dạng VTT
    assData.dialogues.forEach((dialogue, index) => {
        const startTime = formatAssTimeToVTT(dialogue.start);
        const endTime = formatAssTimeToVTT(dialogue.end);
        const text = dialogue.text.replace(/\\N/g, '\n').replace(/\{[^}]*\}/g, '');
        
        vttContent += `${index + 1}\n`;
        vttContent += `${startTime} --> ${endTime}\n`;
        vttContent += `${text}\n\n`;
    });
    
    return vttContent;
}

// Hàm chuyển đổi thời gian từ ASS sang VTT
function formatAssTimeToVTT(seconds) {
    const pad = (num) => num.toString().padStart(2, '0');
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = Math.floor(seconds % 60);
    const ms = Math.floor((seconds % 1) * 1000);
    return `${pad(hours)}:${pad(minutes)}:${pad(secs)}.${ms.toString().padStart(3, '0')}`;
}

// Add function to preview poster
function previewPoster(posterUrl, title) {
    const modal = new bootstrap.Modal(document.getElementById('posterPreviewModal'));
    document.getElementById('posterPreviewImage').src = posterUrl;
    document.querySelector('#posterPreviewModal .modal-title').textContent = `Poster: ${title}`;
    modal.show();
} 