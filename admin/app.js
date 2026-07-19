// Firebase Configuration and Initialization
const firebaseConfig = {
  apiKey: "AIzaSyDfGLNhCwLr_gAxV9LRz0Ppm7Feb-iZYt4",
  authDomain: "studio-1737295542-906cd.firebaseapp.com",
  databaseURL: "https://studio-1737295542-906cd-default-rtdb.firebaseio.com",
  projectId: "studio-1737295542-906cd"
};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);
const database = firebase.database();

// Global State
let animesList = [];
let selectedAnimeId = null;
let editingAnimeId = null;
let editingEpisodeId = null;

// DOM Elements
const animeGrid = document.getElementById('animeGrid');
const searchInput = document.getElementById('searchInput');
const filterStatus = document.getElementById('filterStatus');
const totalAnimeSpan = document.getElementById('totalAnime');
const totalEpisodesSpan = document.getElementById('totalEpisodes');

// Episode UI Elements
const episodePanel = document.getElementById('episodePanel');
const selectedAnimeTitle = document.getElementById('selectedAnimeTitle');
const episodesListDiv = document.getElementById('episodesList');
const episodeFormTitle = document.getElementById('episodeFormTitle');
const btnSaveEpisode = document.getElementById('btnSaveEpisode');

// Modals
const animeModal = document.getElementById('animeModal');
const animeModalTitle = document.getElementById('animeModalTitle');
const animeForm = document.getElementById('animeForm');

// Document Ready
document.addEventListener('DOMContentLoaded', () => {
  setupListeners();
  loadData();
});

// Setup Listeners
function setupListeners() {
  // Search and Filter
  searchInput.addEventListener('input', filterAndRender);
  filterStatus.addEventListener('change', filterAndRender);

  // Forms
  animeForm.addEventListener('submit', handleAnimeFormSubmit);
  document.getElementById('episodeForm').addEventListener('submit', handleEpisodeFormSubmit);
}

// Load Data from Firebase
function loadData() {
  const animeRef = database.ref('Anime');
  
  // Real-time listener for Anime
  animeRef.on('value', (snapshot) => {
    const data = snapshot.val();
    animesList = [];
    
    if (data) {
      Object.keys(data).forEach(id => {
        animesList.push({
          id: id,
          ...data[id]
        });
      });
    }
    
    updateStats();
    filterAndRender();
    
    // If an anime is currently selected, refresh its episode panel
    if (selectedAnimeId) {
      selectAnime(selectedAnimeId);
    }
  }, (error) => {
    console.error("Firebase read error: ", error);
    alert("حدث خطأ أثناء تحميل البيانات من قاعدة البيانات.");
  });
}

// Update Dashboard Statistics
function updateStats() {
  totalAnimeSpan.innerText = animesList.length;
  
  let totalEpisodes = 0;
  animesList.forEach(anime => {
    if (anime.episodes) {
      totalEpisodes += Object.keys(anime.episodes).length;
    }
  });
  totalEpisodesSpan.innerText = totalEpisodes;
}

// Filter and Render Anime Cards
function filterAndRender() {
  const query = searchInput.value.toLowerCase().trim();
  const statusFilter = filterStatus.value;
  
  const filtered = animesList.filter(anime => {
    const matchesSearch = anime.title.toLowerCase().includes(query) || 
                          (anime.category && anime.category.toLowerCase().includes(query)) ||
                          (anime.description && anime.description.toLowerCase().includes(query));
    const matchesStatus = statusFilter === 'all' || anime.status === statusFilter;
    return matchesSearch && matchesStatus;
  });
  
  renderAnimeCards(filtered);
}

// Render Anime Cards Grid
function renderAnimeCards(list) {
  animeGrid.innerHTML = '';
  
  if (list.length === 0) {
    animeGrid.innerHTML = `
      <div class="empty-state" style="grid-column: 1 / -1;">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" stroke-linecap="round"/>
        </svg>
        <p>لم يتم العثور على أي أنمي يطابق معايير البحث.</p>
      </div>
    `;
    return;
  }
  
  list.forEach(anime => {
    const episodeCount = anime.episodes ? Object.keys(anime.episodes).length : 0;
    const coverImage = anime.image || 'https://via.placeholder.com/300x180?text=No+Cover';
    const statusBadge = anime.status === 'مستمر' 
      ? '<span class="badge badge-warning">مستمر</span>' 
      : '<span class="badge badge-success">مكتمل</span>';
      
    const card = document.createElement('div');
    card.className = `anime-card ${selectedAnimeId === anime.id ? 'active' : ''}`;
    card.innerHTML = `
      <img src="${coverImage}" class="anime-card-cover" alt="${anime.title}" onerror="this.src='https://via.placeholder.com/300x180?text=No+Image'">
      <div class="anime-card-content">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 5px;">
          <span class="anime-card-category">${anime.category || 'عام'}</span>
          ${statusBadge}
        </div>
        <h3 class="anime-card-title">${anime.title}</h3>
        <p class="anime-card-description">${anime.description || 'لا يوجد وصف متاح.'}</p>
        <div class="anime-card-meta">
          <span>السنة: ${anime.year || 'غير محدد'}</span>
          <span>الحلقات: <strong>${episodeCount}</strong></span>
        </div>
        <div class="anime-card-actions">
          <button class="btn btn-primary btn-sm" onclick="selectAnime('${anime.id}')">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14" stroke-linecap="round"/></svg> الحلقات
          </button>
          <button class="btn btn-secondary btn-sm" onclick="openEditAnimeModal('${anime.id}')">تعديل</button>
          <button class="btn btn-danger btn-sm" onclick="deleteAnime('${anime.id}')">حذف</button>
        </div>
      </div>
    `;
    animeGrid.appendChild(card);
  });
}

// Open Modal for adding a new Anime
window.openAddAnimeModal = function() {
  editingAnimeId = null;
  animeModalTitle.innerText = "إضافة أنمي جديد";
  animeForm.reset();
  animeModal.style.display = 'flex';
};

// Open Modal for editing Anime details
window.openEditAnimeModal = function(id) {
  editingAnimeId = id;
  const anime = animesList.find(a => a.id === id);
  if (!anime) return;
  
  animeModalTitle.innerText = "تعديل بيانات الأنمي";
  document.getElementById('animeTitle').value = anime.title || '';
  document.getElementById('animeDescription').value = anime.description || '';
  document.getElementById('animeImage').value = anime.image || '';
  document.getElementById('animeCategory').value = anime.category || '';
  document.getElementById('animeYear').value = anime.year || '';
  document.getElementById('animeStatus').value = anime.status || 'مستمر';
  
  animeModal.style.display = 'flex';
};

// Close Anime Modal
window.closeAnimeModal = function() {
  animeModal.style.display = 'none';
};

// Handle Anime Form Submit (Add or Edit)
function handleAnimeFormSubmit(e) {
  e.preventDefault();
  
  const title = document.getElementById('animeTitle').value.trim();
  const description = document.getElementById('animeDescription').value.trim();
  const image = document.getElementById('animeImage').value.trim();
  const category = document.getElementById('animeCategory').value.trim();
  const year = document.getElementById('animeYear').value.trim();
  const status = document.getElementById('animeStatus').value;
  
  if (!title || !description || !image || !category) {
    alert("يرجى ملء جميع الحقول الإلزامية.");
    return;
  }
  
  const animeData = {
    title,
    description,
    image,
    category,
    year,
    status
  };
  
  if (editingAnimeId) {
    // Update existing anime (preserve episodes if any)
    const existing = animesList.find(a => a.id === editingAnimeId);
    if (existing && existing.episodes) {
      animeData.episodes = existing.episodes;
    }
    
    database.ref('Anime/' + editingAnimeId).set(animeData)
      .then(() => {
        closeAnimeModal();
      })
      .catch(err => {
        console.error("Error updating anime:", err);
        alert("فشل تحديث الأنمي.");
      });
  } else {
    // Add new anime
    const newAnimeRef = database.ref('Anime').push();
    newAnimeRef.set(animeData)
      .then(() => {
        closeAnimeModal();
      })
      .catch(err => {
        console.error("Error adding anime:", err);
        alert("فشل إضافة الأنمي.");
      });
  }
}

// Delete Anime entirely
window.deleteAnime = function(id) {
  if (confirm("هل أنت متأكد من رغبتك في حذف هذا الأنمي وجميع حلقاته نهائياً؟")) {
    database.ref('Anime/' + id).remove()
      .then(() => {
        if (selectedAnimeId === id) {
          selectedAnimeId = null;
          episodePanel.style.display = 'none';
        }
      })
      .catch(err => {
        console.error("Error deleting anime:", err);
        alert("فشل حذف الأنمي.");
      });
  }
};

// Select an Anime to manage its episodes
window.selectAnime = function(id) {
  selectedAnimeId = id;
  const anime = animesList.find(a => a.id === id);
  if (!anime) return;
  
  selectedAnimeTitle.innerText = anime.title;
  episodePanel.style.display = 'flex';
  
  // Highlighting active card in list
  filterAndRender();
  
  // Render Episode List
  renderEpisodes(anime.episodes);
  resetEpisodeForm();
};

// Render episodes of selected anime
function renderEpisodes(episodesObj) {
  episodesListDiv.innerHTML = '';
  
  if (!episodesObj) {
    episodesListDiv.innerHTML = `
      <div class="empty-state">
        <p>لا توجد حلقات مضافة لهذا الأنمي حتى الآن. أضف الحلقة الأولى بالأسفل!</p>
      </div>
    `;
    return;
  }
  
  // Convert episodes object to array and sort by episode number
  const list = Object.keys(episodesObj).map(id => ({
    id: id,
    ...episodesObj[id]
  })).sort((a, b) => Number(a.number) - Number(b.number));
  
  list.forEach(ep => {
    const epTitle = ep.title ? ` - ${ep.title}` : '';
    const durationText = ep.duration ? `(${ep.duration})` : '';
    
    const div = document.createElement('div');
    div.className = 'episode-item';
    div.innerHTML = `
      <div class="episode-info">
        <span class="episode-title">الحلقة ${ep.number}${epTitle}</span>
        <span class="episode-meta">المدة: ${durationText || 'غير محدد'} | الرابط: <a href="${ep.video}" target="_blank" style="color: var(--accent-color);">رابط الفيديو</a></span>
      </div>
      <div style="display: flex; gap: 5px;">
        <button class="btn btn-secondary btn-sm" onclick="editEpisode('${ep.id}')">تعديل</button>
        <button class="btn btn-danger btn-sm" onclick="deleteEpisode('${ep.id}')">حذف</button>
      </div>
    `;
    episodesListDiv.appendChild(div);
  });
}

// Submit Episode form (Add or Edit)
function handleEpisodeFormSubmit(e) {
  e.preventDefault();
  
  if (!selectedAnimeId) {
    alert("يرجى اختيار أنمي أولاً.");
    return;
  }
  
  const number = document.getElementById('episodeNumber').value.trim();
  const title = document.getElementById('episodeTitle').value.trim();
  const video = document.getElementById('episodeVideo').value.trim();
  const duration = document.getElementById('episodeDuration').value.trim();
  
  if (!number || !video) {
    alert("رقم الحلقة ورابط الفيديو حقول إلزامية.");
    return;
  }
  
  const episodeData = {
    number,
    title,
    video,
    duration
  };
  
  if (editingEpisodeId) {
    // Update episode
    database.ref(`Anime/${selectedAnimeId}/episodes/${editingEpisodeId}`).set(episodeData)
      .then(() => {
        resetEpisodeForm();
      })
      .catch(err => {
        console.error("Error updating episode:", err);
        alert("فشل تحديث الحلقة.");
      });
  } else {
    // Add new episode
    database.ref(`Anime/${selectedAnimeId}/episodes`).push(episodeData)
      .then(() => {
        resetEpisodeForm();
      })
      .catch(err => {
        console.error("Error adding episode:", err);
        alert("فشل إضافة الحلقة.");
      });
  }
}

// Edit Episode details
window.editEpisode = function(epId) {
  if (!selectedAnimeId) return;
  const anime = animesList.find(a => a.id === selectedAnimeId);
  if (!anime || !anime.episodes || !anime.episodes[epId]) return;
  
  const ep = anime.episodes[epId];
  editingEpisodeId = epId;
  
  episodeFormTitle.innerText = "تعديل الحلقة " + ep.number;
  document.getElementById('episodeNumber').value = ep.number || '';
  document.getElementById('episodeTitle').value = ep.title || '';
  document.getElementById('episodeVideo').value = ep.video || '';
  document.getElementById('episodeDuration').value = ep.duration || '';
  btnSaveEpisode.innerText = "تعديل الحلقة";
};

// Delete Episode
window.deleteEpisode = function(epId) {
  if (confirm("هل أنت متأكد من رغبتك في حذف هذه الحلقة؟")) {
    database.ref(`Anime/${selectedAnimeId}/episodes/${epId}`).remove()
      .then(() => {
        console.log("Episode deleted");
      })
      .catch(err => {
        console.error("Error deleting episode:", err);
        alert("فشل حذف الحلقة.");
      });
  }
};

// Reset Episode form back to Add state
function resetEpisodeForm() {
  editingEpisodeId = null;
  episodeFormTitle.innerText = "إضافة حلقة جديدة";
  document.getElementById('episodeForm').reset();
  btnSaveEpisode.innerText = "إضافة الحلقة";
}
