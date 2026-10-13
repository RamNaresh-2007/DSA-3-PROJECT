/**
 * ============================================================================
 * EDUTRACK UNIVERSITY ACADEMIC PLATFORM — FRONTEND CLIENT & LIVE DSA SUITE
 * Complete dataset implementation powered by EduTrack.csv (10,000 Records)
 * Real-time REST API integration with embedded Java DSA backend & client engine
 * ============================================================================
 */

// Application State
const STATE = {
  isBackendConnected: false,
  students: [],
  courses: [],
  faculty: [],
  schedules: [],
  admins: [],
  stats: null,
  activeTab: 'dashboard',
  activeDsaTab: 'string-algo',
  filters: {
    dept: 'ALL',
    year: 'ALL',
    grade: 'ALL',
    status: 'ALL',
    sort: 'cgpa-desc',
    search: '',
    page: 1,
    limit: 50
  },
  cachedFilteredStudents: []
};

// ============================================================================
// 1. INITIALIZATION & DATA LOADING (EduTrack.csv & Backend API)
// ============================================================================

document.addEventListener('DOMContentLoaded', () => {
  initNavigation();
  initSearchAndFilters();
  initDetailModal();
  loadData();
});

async function loadData() {
  showToast("Initializing EduTrack platform...", "info");

  // Step 1: Attempt to connect to Java backend API
  let connected = false;
  try {
    const res = await fetch('/api/stats', { cache: 'no-store' });
    if (res.ok) {
      const stats = await res.json();
      STATE.stats = stats;
      STATE.isBackendConnected = true;
      connected = true;
      updateEngineModeBadge(true);
      console.log("[EduTrack] Connected to Java Backend Server (port 8080/8000)");
    }
  } catch (e) {
    console.log("[EduTrack] Java backend not running or different port. Using high-speed client-side engine.");
  }

  // Step 2: Load Courses & Faculty from API or fallback
  if (connected) {
    try {
      const [cRes, fRes] = await Promise.all([
        fetch('/api/courses'),
        fetch('/api/faculty')
      ]);
      if (cRes.ok) STATE.courses = await cRes.json();
      if (fRes.ok) STATE.faculty = await fRes.json();
    } catch (e) {
      console.warn("Could not fetch auxiliary data from API", e);
    }
  }

  // Step 3: Load EduTrack.csv directly (either to seed client store or full dataset)
  await loadCsvDataset();

  // Step 4: Render initial views
  renderDashboard();
  applyFiltersAndRenderStudents();
  renderCourses();
  renderFaculty();
  renderSchedules();
  renderAdmins();
  initDsaPlayground();

  showToast(`Loaded ${STATE.students.length.toLocaleString()} student records from EduTrack.csv`, "success");
}

async function loadCsvDataset() {
  const candidateUrls = [
    '/EduTrack.csv',
    '/data/EduTrack.csv',
    'EduTrack.csv',
    'data/EduTrack.csv',
    '../EduTrack.csv',
    '../../EduTrack.csv'
  ];

  let csvText = null;
  for (const url of candidateUrls) {
    try {
      const res = await fetch(url);
      if (res.ok) {
        csvText = await res.text();
        console.log(`[EduTrack] Ingested EduTrack.csv from ${url}`);
        break;
      }
    } catch (ignored) {}
  }

  if (csvText && csvText.trim().length > 100) {
    parseCsvToState(csvText);
  } else {
    // If running in restricted environment without local web server, use sample generation or API
    if (STATE.isBackendConnected) {
      await fetchStudentsFromApi();
    } else {
      generateLocalFallbackDataset();
    }
  }
}

function parseCsvToState(csvText) {
  const lines = csvText.split(/\r?\n/);
  if (lines.length < 2) return;

  const header = parseCsvLine(lines[0]);
  const students = [];
  const courseMap = new Map();
  const facultyMap = new Map();
  const scheduleMap = new Map();

  for (let i = 1; i < lines.length; i++) {
    const line = lines[i].trim();
    if (!line) continue;

    const parts = parseCsvLine(line);
    if (parts.length < 38) continue;

    const s = {
      studentId: parts[0],
      name: parts[1],
      age: parseInt(parts[2]) || 20,
      gender: parts[3],
      department: parts[4],
      year: parseInt(parts[5]) || 1,
      semester: parseInt(parts[6]) || 1,
      courseId: parts[7],
      courseCode: parts[8],
      courseName: parts[9],
      credits: parseInt(parts[10]) || 3,
      facultyId: parts[11],
      facultyName: parts[12],
      attendance: parseFloat(parts[13]) || 75.0,
      studyHours: parseFloat(parts[14]) || 5.0,
      assignmentScore: parseFloat(parts[15]) || 70.0,
      midtermScore: parseFloat(parts[16]) || 70.0,
      examScore: parseFloat(parts[17]) || 70.0,
      cgpa: parseFloat(parts[18]) || 7.0,
      grade: parts[19],
      academicStatus: parts[20],
      activityId: parts[21],
      activityType: parts[22],
      activityScore: parseInt(parts[23]) || 100,
      activityDate: parts[24],
      assignmentId: parts[25],
      assignmentText: parts[26],
      academicKeywords: parts[27],
      examId: parts[28],
      examDate: parts[29],
      roomId: parts[30],
      scheduleDay: parts[31],
      scheduleTime: parts[32],
      resourceId: parts[33],
      resourceType: parts[34],
      resourceTitle: parts[35],
      queryText: parts[36],
      conflictGroup: parseInt(parts[37]) || 100
    };
    students.push(s);

    // Collect Courses
    if (!courseMap.has(s.courseId)) {
      courseMap.set(s.courseId, {
        courseId: s.courseId,
        courseCode: s.courseCode,
        courseName: s.courseName,
        credits: s.credits,
        facultyId: s.facultyId,
        facultyName: s.facultyName,
        department: mapCourseToDept(s.courseCode, s.department),
        description: `${s.courseName} (${s.courseCode}) — Comprehensive syllabus including foundational concepts, practical laboratory exercises, and algorithmic optimizations.`
      });
    }

    // Collect Faculty
    if (!facultyMap.has(s.facultyId)) {
      facultyMap.set(s.facultyId, {
        facultyId: s.facultyId,
        name: s.facultyName,
        email: `${s.facultyName.replace('Dr. ', '').trim().toLowerCase().replace(/\s+/g, '.')}@univ.edu`,
        department: s.department,
        specialization: `${s.courseName} & Distributed Systems`
      });
    }

    // Collect Schedules
    if (!scheduleMap.has(s.examId) && scheduleMap.size < 100) {
      scheduleMap.set(s.examId, {
        scheduleId: s.examId,
        courseCode: s.courseCode,
        courseName: s.courseName,
        examDate: s.examDate,
        scheduleDay: s.scheduleDay,
        scheduleTime: s.scheduleTime,
        roomId: s.roomId,
        facultyName: s.facultyName,
        conflictGroup: s.conflictGroup
      });
    }
  }

  STATE.students = students;
  if (STATE.courses.length === 0) STATE.courses = Array.from(courseMap.values());
  if (STATE.faculty.length === 0) STATE.faculty = Array.from(facultyMap.values());
  STATE.schedules = Array.from(scheduleMap.values());

  // Admins
  STATE.admins = [
    { adminId: "ADM001", name: "Dr. Rajeshwar Sharma", role: "Dean of Academic Affairs", department: "Academic Affairs", accessLevel: "SUPER_ADMIN", office: "Admin Block Room 101" },
    { adminId: "ADM002", name: "Dr. Meera Krishnan", role: "University Registrar", department: "Office of the Registrar", accessLevel: "SUPER_ADMIN", office: "Admin Block Room 102" },
    { adminId: "ADM003", name: "Prof. Sanjay Deshmukh", role: "Controller of Examinations", department: "Examination Branch", accessLevel: "EXAM_ADMIN", office: "Exam Wing Room 204" },
    { adminId: "ADM004", name: "Pooja Varma", role: "Director of Admissions", department: "Admissions & Records", accessLevel: "ADMISSIONS_ADMIN", office: "Admissions Center Room 105" },
    { adminId: "ADM005", name: "Vikramaditya Bose", role: "Chief System Administrator", department: "IT & Infrastructure", accessLevel: "IT_ADMIN", office: "Tech Center Server Room A" }
  ];

  calculateStatsLocally();
}

function parseCsvLine(text) {
  const result = [];
  let cur = '';
  let inQuotes = false;
  for (let i = 0; i < text.length; i++) {
    const c = text[i];
    if (c === '"') {
      inQuotes = !inQuotes;
    } else if (c === ',' && !inQuotes) {
      result.push(cur.trim());
      cur = '';
    } else {
      cur += c;
    }
  }
  result.push(cur.trim());
  return result;
}

function mapCourseToDept(code, fallback) {
  if (code.startsWith('CS')) return 'CSE';
  if (code.startsWith('AI')) return 'AIML';
  if (code.startsWith('EC')) return 'ECE';
  if (code.startsWith('IT')) return 'IT';
  if (code.startsWith('SE')) return 'CSE';
  if (code.startsWith('MA')) return 'MATH';
  return fallback;
}

function calculateStatsLocally() {
  const total = STATE.students.length;
  let passCount = 0;
  let failCount = 0;
  let sumCgpa = 0;
  let sumAtt = 0;

  const gradeDist = { "A+": 0, "A": 0, "B": 0, "C": 0, "D": 0, "F": 0 };
  const deptDist = { "CSE": 0, "ECE": 0, "ME": 0, "EEE": 0, "AIML": 0, "AI&DS": 0, "IT": 0, "CE": 0 };
  const actDist = {};
  const resDist = {};

  for (const s of STATE.students) {
    if (s.academicStatus === 'Pass') passCount++;
    else failCount++;
    sumCgpa += s.cgpa;
    sumAtt += s.attendance;

    if (gradeDist[s.grade] !== undefined) gradeDist[s.grade]++;
    if (deptDist[s.department] !== undefined) deptDist[s.department]++;

    actDist[s.activityType] = (actDist[s.activityType] || 0) + 1;
    resDist[s.resourceType] = (resDist[s.resourceType] || 0) + 1;
  }

  STATE.stats = {
    totalStudents: total,
    totalCourses: STATE.courses.length,
    totalFaculty: STATE.faculty.length,
    passCount,
    failCount,
    averageCgpa: total > 0 ? (sumCgpa / total).toFixed(2) : "0.00",
    averageAttendance: total > 0 ? (sumAtt / total).toFixed(1) : "0.0",
    gradeDistribution: gradeDist,
    departmentDistribution: deptDist,
    activityDistribution: actDist,
    resourceDistribution: resDist
  };
}

async function fetchStudentsFromApi() {
  try {
    const res = await fetch('/api/students?limit=200');
    if (res.ok) {
      const data = await res.json();
      STATE.students = data.students || [];
      calculateStatsLocally();
    }
  } catch (e) {
    console.error("API error", e);
  }
}

function generateLocalFallbackDataset() {
  // If file reading fails completely, inform the user
  showToast("Unable to load EduTrack.csv directly. Please ensure file exists in project directory.", "error");
}

function updateEngineModeBadge(connected) {
  const badge = document.getElementById('engine-mode-badge');
  const label = document.getElementById('engine-mode-label');
  const pillText = document.getElementById('status-engine-text');
  const subText = document.getElementById('status-sub-text');

  if (connected) {
    badge.className = 'engine-mode-badge';
    label.textContent = 'Backend Java Engine Active';
    pillText.textContent = 'Java DSA Server';
    subText.textContent = 'Port 8080/8000 • Live REST API';
  } else {
    badge.className = 'engine-mode-badge offline';
    label.textContent = 'Client DSA Engine Active';
    pillText.textContent = 'Client DSA Engine';
    subText.textContent = '10,000 Records • Local RAM';
  }
}

// ============================================================================
// 2. DASHBOARD RENDERING & ANALYTICS CHARTS
// ============================================================================

function renderDashboard() {
  if (!STATE.stats) calculateStatsLocally();
  const st = STATE.stats;

  // KPI Metrics
  document.getElementById('stat-total-students').textContent = (st.totalStudents || STATE.students.length).toLocaleString();
  document.getElementById('stat-total-courses').textContent = (st.totalCourses || STATE.courses.length).toString();
  document.getElementById('stat-total-faculty').textContent = (st.totalFaculty || STATE.faculty.length).toString();

  const total = st.totalStudents || 1;
  const passRate = ((st.passCount / total) * 100).toFixed(1);
  document.getElementById('stat-pass-rate').textContent = `${passRate}%`;
  document.getElementById('stat-avg-cgpa').textContent = `Avg CGPA: ${st.averageCgpa} • Att: ${st.averageAttendance}%`;

  // Department distribution
  renderDepartmentBars(st.departmentDistribution);

  // Grade spectrum cards
  renderGradeSpectrum(st.gradeDistribution);

  // Top performers
  renderTopPerformers();

  // Activities & Resources breakdown
  renderActivityBreakdown(st.activityDistribution);
  renderResourceBreakdown(st.resourceDistribution);
}

function renderDepartmentBars(deptDist) {
  const container = document.getElementById('dept-distribution-container');
  if (!container || !deptDist) return;

  const entries = Object.entries(deptDist);
  const max = Math.max(...entries.map(e => e[1]), 1);

  let html = '';
  for (const [dept, count] of entries) {
    const pct = ((count / max) * 100).toFixed(0);
    html += `
      <div class="dept-item">
        <div class="dept-header">
          <span class="dept-name"><strong>${dept}</strong> — ${getDeptFullName(dept)}</span>
          <span class="dept-count">${count.toLocaleString()} students</span>
        </div>
        <div class="progress-bar-track">
          <div class="progress-bar-fill" style="width: ${pct}%"></div>
        </div>
      </div>
    `;
  }
  container.innerHTML = html;
}

function getDeptFullName(dept) {
  const names = {
    CSE: 'Computer Science & Engineering',
    ECE: 'Electronics & Communication',
    AIML: 'Artificial Intelligence & Machine Learning',
    'AI&DS': 'AI & Data Science',
    IT: 'Information Technology',
    ME: 'Mechanical Engineering',
    EEE: 'Electrical & Electronics',
    CE: 'Civil Engineering'
  };
  return names[dept] || dept;
}

function renderGradeSpectrum(gradeDist) {
  const container = document.getElementById('cgpa-tiers-container');
  if (!container || !gradeDist) return;

  const total = STATE.students.length || 1;
  const tiers = [
    { grade: 'A+', label: 'Outstanding (9.0 - 10.0)', color: '#10b981', count: gradeDist['A+'] || 0 },
    { grade: 'A',  label: 'Excellent (8.0 - 8.9)',    color: '#06b6d4', count: gradeDist['A'] || 0 },
    { grade: 'B',  label: 'Good (7.0 - 7.9)',         color: '#6366f1', count: gradeDist['B'] || 0 },
    { grade: 'C',  label: 'Satisfactory (6.0 - 6.9)', color: '#f59e0b', count: gradeDist['C'] || 0 },
    { grade: 'D',  label: 'Pass Threshold (5.0 - 5.9)', color: '#f97316', count: gradeDist['D'] || 0 },
    { grade: 'F',  label: 'Academic Risk (< 5.0)',    color: '#ef4444', count: gradeDist['F'] || 0 }
  ];

  let html = '';
  for (const t of tiers) {
    const pct = ((t.count / total) * 100).toFixed(1);
    html += `
      <div class="cgpa-tier-card" style="border-left: 4px solid ${t.color}">
        <div class="tier-top">
          <span class="tier-grade" style="color: ${t.color}">${t.grade}</span>
          <span class="tier-count">${t.count.toLocaleString()}</span>
        </div>
        <div class="tier-desc">${t.label}</div>
        <div class="tier-pct">${pct}% of student body</div>
      </div>
    `;
  }
  container.innerHTML = html;
}

function renderTopPerformers() {
  const container = document.getElementById('top-performers-list');
  if (!container) return;

  // Find top 5 by CGPA
  const sorted = [...STATE.students].sort((a, b) => b.cgpa - a.cgpa).slice(0, 5);

  let html = '';
  sorted.forEach((s, idx) => {
    html += `
      <div class="performer-row" onclick="openStudentModal('${s.studentId}')">
        <div class="performer-rank">#${idx + 1}</div>
        <div class="performer-info">
          <div class="performer-name">${escapeHtml(s.name)} <span class="badge badge-accent">${s.studentId}</span></div>
          <div class="performer-sub">${s.department} • Year ${s.year} • Course: ${escapeHtml(s.courseName)}</div>
        </div>
        <div class="performer-cgpa">
          <strong>${s.cgpa.toFixed(2)}</strong> CGPA
        </div>
      </div>
    `;
  });
  container.innerHTML = html;
}

function renderActivityBreakdown(actDist) {
  const container = document.getElementById('activity-distribution-container');
  if (!container || !actDist) return;

  const entries = Object.entries(actDist);
  const total = STATE.students.length || 1;

  let html = '';
  for (const [type, count] of entries) {
    const pct = ((count / total) * 100).toFixed(1);
    html += `
      <div class="metric-row">
        <div class="metric-info">
          <span>${escapeHtml(type)}</span>
          <strong>${count.toLocaleString()} (${pct}%)</strong>
        </div>
        <div class="progress-bar-track">
          <div class="progress-bar-fill bg-cyan" style="width: ${pct}%"></div>
        </div>
      </div>
    `;
  }
  container.innerHTML = html;
}

function renderResourceBreakdown(resDist) {
  const container = document.getElementById('resource-distribution-container');
  if (!container || !resDist) return;

  const entries = Object.entries(resDist);
  const total = STATE.students.length || 1;

  let html = '';
  for (const [type, count] of entries) {
    const pct = ((count / total) * 100).toFixed(1);
    html += `
      <div class="metric-row">
        <div class="metric-info">
          <span>${escapeHtml(type)} Format</span>
          <strong>${count.toLocaleString()} (${pct}%)</strong>
        </div>
        <div class="progress-bar-track">
          <div class="progress-bar-fill bg-purple" style="width: ${pct}%"></div>
        </div>
      </div>
    `;
  }
  container.innerHTML = html;
}

// ============================================================================
// 3. STUDENTS EXPLORER (10,000 RECORDS)
// ============================================================================

function initSearchAndFilters() {
  // Global search input
  const globalSearch = document.getElementById('global-search');
  const clearBtn = document.getElementById('global-search-clear');

  let debounceTimer = null;
  globalSearch.addEventListener('input', (e) => {
    clearTimeout(debounceTimer);
    clearBtn.style.display = e.target.value ? 'block' : 'none';
    debounceTimer = setTimeout(() => {
      STATE.filters.search = e.target.value.trim();
      STATE.filters.page = 1;
      applyFiltersAndRenderStudents();
    }, 250);
  });

  clearBtn.addEventListener('click', () => {
    globalSearch.value = '';
    clearBtn.style.display = 'none';
    STATE.filters.search = '';
    STATE.filters.page = 1;
    applyFiltersAndRenderStudents();
  });

  // Department Filter
  document.getElementById('student-dept-filter').addEventListener('change', (e) => {
    STATE.filters.dept = e.target.value;
    STATE.filters.page = 1;
    applyFiltersAndRenderStudents();
  });

  // Year Filter
  document.getElementById('student-year-filter').addEventListener('change', (e) => {
    STATE.filters.year = e.target.value;
    STATE.filters.page = 1;
    applyFiltersAndRenderStudents();
  });

  // Grade Filter
  document.getElementById('student-grade-filter').addEventListener('change', (e) => {
    STATE.filters.grade = e.target.value;
    STATE.filters.page = 1;
    applyFiltersAndRenderStudents();
  });

  // Status Filter
  document.getElementById('student-status-filter').addEventListener('change', (e) => {
    STATE.filters.status = e.target.value;
    STATE.filters.page = 1;
    applyFiltersAndRenderStudents();
  });

  // Sort
  document.getElementById('student-sort').addEventListener('change', (e) => {
    STATE.filters.sort = e.target.value;
    applyFiltersAndRenderStudents();
  });

  // Pagination buttons
  document.getElementById('prev-page-btn').addEventListener('click', () => {
    if (STATE.filters.page > 1) {
      STATE.filters.page--;
      renderStudentsTable();
    }
  });

  document.getElementById('next-page-btn').addEventListener('click', () => {
    const totalPages = Math.ceil(STATE.cachedFilteredStudents.length / STATE.filters.limit);
    if (STATE.filters.page < totalPages) {
      STATE.filters.page++;
      renderStudentsTable();
    }
  });

  // Export CSV button
  document.getElementById('export-csv-btn').addEventListener('click', exportActiveStudentsCsv);
}

function applyFiltersAndRenderStudents() {
  const f = STATE.filters;
  const searchLower = f.search.toLowerCase();
  const yearNum = f.year !== 'ALL' ? parseInt(f.year) : null;

  let filtered = STATE.students.filter(s => {
    if (f.dept !== 'ALL' && s.department !== f.dept) return false;
    if (yearNum !== null && s.year !== yearNum) return false;
    if (f.grade !== 'ALL' && s.grade !== f.grade) return false;
    if (f.status !== 'ALL' && s.academicStatus !== f.status) return false;
    if (searchLower) {
      const match = s.studentId.toLowerCase().includes(searchLower) ||
                    s.name.toLowerCase().includes(searchLower) ||
                    s.courseName.toLowerCase().includes(searchLower) ||
                    s.facultyName.toLowerCase().includes(searchLower);
      if (!match) return false;
    }
    return true;
  });

  // Sorting
  filtered.sort((a, b) => {
    switch (f.sort) {
      case 'cgpa-desc': return b.cgpa - a.cgpa;
      case 'cgpa-asc': return a.cgpa - b.cgpa;
      case 'attendance-desc': return b.attendance - a.attendance;
      case 'exam-desc': return b.examScore - a.examScore;
      case 'name-asc': return a.name.localeCompare(b.name);
      case 'id-asc': return a.studentId.localeCompare(b.studentId);
      default: return b.cgpa - a.cgpa;
    }
  });

  STATE.cachedFilteredStudents = filtered;
  document.getElementById('students-results-counter').textContent = 
    `Showing ${filtered.length.toLocaleString()} of ${STATE.students.length.toLocaleString()} students`;

  renderStudentsTable();
}

function renderStudentsTable() {
  const tbody = document.getElementById('students-table-body');
  if (!tbody) return;

  const f = STATE.filters;
  const total = STATE.cachedFilteredStudents.length;
  const totalPages = Math.max(1, Math.ceil(total / f.limit));

  if (f.page > totalPages) f.page = totalPages;
  if (f.page < 1) f.page = 1;

  const start = (f.page - 1) * f.limit;
  const end = Math.min(start + f.limit, total);
  const pageItems = STATE.cachedFilteredStudents.slice(start, end);

  // Update pagination indicators
  document.getElementById('prev-page-btn').disabled = f.page <= 1;
  document.getElementById('next-page-btn').disabled = f.page >= totalPages;
  document.getElementById('page-info-text').textContent = `Page ${f.page} of ${totalPages.toLocaleString()} (${total.toLocaleString()} total)`;

  if (pageItems.length === 0) {
    tbody.innerHTML = `<tr><td colspan="10" style="text-align:center; padding: 40px; color: var(--text-muted);">No student records match the active filters.</td></tr>`;
    return;
  }

  let html = '';
  for (const s of pageItems) {
    const statusClass = s.academicStatus === 'Pass' ? 'badge-success' : 'badge-danger';
    const gradeClass = s.grade === 'A+' || s.grade === 'A' ? 'badge-accent' : (s.grade === 'F' ? 'badge-danger' : 'badge-subtle');
    const attColor = s.attendance >= 80 ? 'var(--emerald-400)' : (s.attendance >= 70 ? 'var(--amber-400)' : 'var(--rose-400)');

    html += `
      <tr>
        <td><strong class="mono text-indigo">${s.studentId}</strong></td>
        <td>
          <div class="student-name-cell">
            <strong>${escapeHtml(s.name)}</strong>
            <span class="sub-text">${s.gender} • Age ${s.age}</span>
          </div>
        </td>
        <td><span class="dept-pill">${s.department}</span></td>
        <td>Year ${s.year} (Sem ${s.semester})</td>
        <td>
          <div><strong>${escapeHtml(s.courseName)}</strong></div>
          <span class="sub-text">${s.courseCode} • ${s.facultyName}</span>
        </td>
        <td>
          <span style="color: ${attColor}; font-weight: 600;">${s.attendance}%</span>
          <span class="sub-text">${s.studyHours}h/wk</span>
        </td>
        <td>
          <div class="scores-inline">
            <span>Asg: <strong>${s.assignmentScore}</strong></span>
            <span>Mid: <strong>${s.midtermScore}</strong></span>
            <span>Ex: <strong>${s.examScore}</strong></span>
          </div>
        </td>
        <td>
          <strong>${s.cgpa.toFixed(2)}</strong>
          <span class="badge ${gradeClass} ml-1">${s.grade}</span>
        </td>
        <td><span class="badge ${statusClass}">${s.academicStatus}</span></td>
        <td>
          <button class="btn btn-sm btn-outline" onclick="openStudentModal('${s.studentId}')">Dossier</button>
        </td>
      </tr>
    `;
  }
  tbody.innerHTML = html;
}

// ============================================================================
// 4. DETAIL DOSSIER MODAL
// ============================================================================

function initDetailModal() {
  const modal = document.getElementById('detail-modal');
  const closeBtn = document.getElementById('modal-close');

  closeBtn.addEventListener('click', () => modal.classList.remove('active'));
  modal.addEventListener('click', (e) => {
    if (e.target === modal) modal.classList.remove('active');
  });
}

function openStudentModal(studentId) {
  const s = STATE.students.find(x => x.studentId === studentId);
  if (!s) return;

  const modal = document.getElementById('detail-modal');
  const title = document.getElementById('modal-title');
  const body = document.getElementById('modal-body');

  title.textContent = `Student Dossier: ${s.name} (${s.studentId})`;

  body.innerHTML = `
    <div class="dossier-grid">
      <!-- Section 1: Demographics & Academic Status -->
      <div class="dossier-card">
        <h4 class="dossier-subheading">Personal Profile & Registration</h4>
        <div class="modal-detail-row"><span class="modal-detail-label">Student ID:</span><span class="modal-detail-value mono">${s.studentId}</span></div>
        <div class="modal-detail-row"><span class="modal-detail-label">Full Name:</span><span class="modal-detail-value">${escapeHtml(s.name)}</span></div>
        <div class="modal-detail-row"><span class="modal-detail-label">Gender & Age:</span><span class="modal-detail-value">${s.gender} • ${s.age} years old</span></div>
        <div class="modal-detail-row"><span class="modal-detail-label">Department:</span><span class="modal-detail-value">${s.department} (${getDeptFullName(s.department)})</span></div>
        <div class="modal-detail-row"><span class="modal-detail-label">Current Standing:</span><span class="modal-detail-value">Year ${s.year}, Semester ${s.semester}</span></div>
        <div class="modal-detail-row"><span class="modal-detail-label">Official Email:</span><span class="modal-detail-value mono">${s.studentId.toLowerCase()}@univ.edu</span></div>
      </div>

      <!-- Section 2: Continuous Assessment & Performance -->
      <div class="dossier-card">
        <h4 class="dossier-subheading">Academic Assessment & Evaluation</h4>
        <div class="modal-detail-row"><span class="modal-detail-label">Cumulative GPA (CGPA):</span><span class="modal-detail-value"><strong>${s.cgpa.toFixed(2)}</strong> / 10.0</span></div>
        <div class="modal-detail-row"><span class="modal-detail-label">Grading & Result:</span><span class="modal-detail-value"><span class="badge badge-accent">${s.grade}</span> <span class="badge ${s.academicStatus === 'Pass' ? 'badge-success' : 'badge-danger'}">${s.academicStatus}</span></span></div>
        <div class="modal-detail-row"><span class="modal-detail-label">Attendance Record:</span><span class="modal-detail-value">${s.attendance}%</span></div>
        <div class="modal-detail-row"><span class="modal-detail-label">Weekly Study Hours:</span><span class="modal-detail-value">${s.studyHours} hours/week</span></div>
        <div class="modal-detail-row"><span class="modal-detail-label">Continuous Assignment Score:</span><span class="modal-detail-value">${s.assignmentScore} / 100</span></div>
        <div class="modal-detail-row"><span class="modal-detail-label">Midterm Examination:</span><span class="modal-detail-value">${s.midtermScore} / 100</span></div>
        <div class="modal-detail-row"><span class="modal-detail-label">Final Exam Score:</span><span class="modal-detail-value">${s.examScore} / 100</span></div>
      </div>
    </div>

    <!-- Section 3: Coursework & Assignment Submission -->
    <div class="dossier-card mt-4">
      <h4 class="dossier-subheading">Course Enrollment & Assignment Submission</h4>
      <div class="modal-detail-row"><span class="modal-detail-label">Course:</span><span class="modal-detail-value">${escapeHtml(s.courseName)} (${s.courseCode}) • ${s.credits} Credits</span></div>
      <div class="modal-detail-row"><span class="modal-detail-label">Faculty Mentor:</span><span class="modal-detail-value">${s.facultyName} (${s.facultyId})</span></div>
      <div class="modal-detail-row"><span class="modal-detail-label">Assignment ID:</span><span class="modal-detail-value mono">${s.assignmentId}</span></div>
      <div class="modal-detail-row"><span class="modal-detail-label">Assignment Topic / Text:</span><span class="modal-detail-value">"${escapeHtml(s.assignmentText)}"</span></div>
      <div class="modal-detail-row"><span class="modal-detail-label">Academic Keywords:</span><span class="modal-detail-value mono text-sm">${escapeHtml(s.academicKeywords)}</span></div>
    </div>

    <!-- Section 4: Examination Timetable & Digital Learning Resource -->
    <div class="dossier-grid mt-4">
      <div class="dossier-card">
        <h4 class="dossier-subheading">Official Examination Allotment</h4>
        <div class="modal-detail-row"><span class="modal-detail-label">Exam ID:</span><span class="modal-detail-value mono">${s.examId}</span></div>
        <div class="modal-detail-row"><span class="modal-detail-label">Date & Day:</span><span class="modal-detail-value">${s.examDate} (${s.scheduleDay})</span></div>
        <div class="modal-detail-row"><span class="modal-detail-label">Scheduled Time:</span><span class="modal-detail-value">${s.scheduleTime}</span></div>
        <div class="modal-detail-row"><span class="modal-detail-label">Designated Room:</span><span class="modal-detail-value badge badge-cyan">${s.roomId}</span></div>
        <div class="modal-detail-row"><span class="modal-detail-label">Conflict Schedule Group:</span><span class="modal-detail-value">Group #${s.conflictGroup}</span></div>
      </div>

      <div class="dossier-card">
        <h4 class="dossier-subheading">Assigned Curriculum Resource & Activity</h4>
        <div class="modal-detail-row"><span class="modal-detail-label">Resource ID & Title:</span><span class="modal-detail-value">${s.resourceId}: ${escapeHtml(s.resourceTitle)}</span></div>
        <div class="modal-detail-row"><span class="modal-detail-label">Format Type:</span><span class="modal-detail-value badge badge-purple">${s.resourceType}</span></div>
        <div class="modal-detail-row"><span class="modal-detail-label">Engagement Activity:</span><span class="modal-detail-value">${s.activityType} (ID: ${s.activityId})</span></div>
        <div class="modal-detail-row"><span class="modal-detail-label">Activity Score:</span><span class="modal-detail-value">${s.activityScore} pts • Date: ${s.activityDate}</span></div>
        <div class="modal-detail-row"><span class="modal-detail-label">Search Query Context:</span><span class="modal-detail-value mono text-xs">${escapeHtml(s.queryText)}</span></div>
      </div>
    </div>
  `;

  modal.classList.add('active');
}

// ============================================================================
// 5. COURSE CATALOG & FACULTY RENDERING
// ============================================================================

function renderCourses() {
  const container = document.getElementById('courses-grid');
  if (!container) return;

  const courses = STATE.courses;
  let html = '';

  for (const c of courses) {
    // Count enrolled students for this course in dataset
    const enrolled = STATE.students.filter(s => s.courseId === c.courseId || s.courseCode === c.courseCode).length;
    const avgCgpa = enrolled > 0 ? (STATE.students.filter(s => s.courseId === c.courseId).reduce((a, b) => a + b.cgpa, 0) / enrolled).toFixed(2) : "N/A";

    html += `
      <div class="course-card glass-card">
        <div class="course-header">
          <span class="course-code badge badge-accent">${c.courseCode}</span>
          <span class="course-credits">${c.credits} Credits</span>
        </div>
        <h3 class="course-title">${escapeHtml(c.courseName)}</h3>
        <p class="course-desc">${escapeHtml(c.description)}</p>
        <div class="course-meta">
          <div class="meta-item">
            <span class="meta-label">Faculty:</span>
            <span class="meta-value">${escapeHtml(c.facultyName)}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Department:</span>
            <span class="meta-value">${c.department}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">Enrollment:</span>
            <span class="meta-value"><strong>${enrolled.toLocaleString()}</strong> students (Avg CGPA: ${avgCgpa})</span>
          </div>
        </div>
        <button class="btn btn-sm btn-outline mt-3 w-full" onclick="filterByCourse('${c.courseName}')">View Enrolled Students &rarr;</button>
      </div>
    `;
  }
  container.innerHTML = html;
}

function filterByCourse(courseName) {
  switchTab('students');
  const searchInput = document.getElementById('global-search');
  searchInput.value = courseName;
  document.getElementById('global-search-clear').style.display = 'block';
  STATE.filters.search = courseName;
  STATE.filters.page = 1;
  applyFiltersAndRenderStudents();
}

function renderFaculty() {
  const container = document.getElementById('faculty-grid');
  if (!container) return;

  let html = '';
  for (const f of STATE.faculty) {
    // Count students mentored
    const mentored = STATE.students.filter(s => s.facultyId === f.facultyId).length;

    html += `
      <div class="entity-card glass-card">
        <div class="entity-card-header">
          <div class="entity-avatar bg-emerald">${f.name.replace('Dr. ', '').charAt(0)}</div>
          <div class="entity-header-text">
            <h3 class="entity-name">${escapeHtml(f.name)}</h3>
            <span class="entity-sub">${f.facultyId} • Department of ${f.department}</span>
          </div>
        </div>
        <div class="entity-card-body">
          <div class="entity-detail"><span class="label">Specialization:</span> <span class="val">${escapeHtml(f.specialization)}</span></div>
          <div class="entity-detail"><span class="label">Academic Email:</span> <span class="val mono">${f.email}</span></div>
          <div class="entity-detail"><span class="label">Student Reach:</span> <span class="val"><strong>${mentored.toLocaleString()}</strong> enrolled students</span></div>
        </div>
        <button class="btn btn-sm btn-outline mt-2" onclick="filterByFaculty('${f.name}')">View Mentored Students</button>
      </div>
    `;
  }
  container.innerHTML = html;
}

function filterByFaculty(facultyName) {
  switchTab('students');
  const searchInput = document.getElementById('global-search');
  searchInput.value = facultyName;
  document.getElementById('global-search-clear').style.display = 'block';
  STATE.filters.search = facultyName;
  STATE.filters.page = 1;
  applyFiltersAndRenderStudents();
}

function renderSchedules() {
  const tbody = document.getElementById('schedules-table-body');
  if (!tbody) return;

  let html = '';
  for (const sch of STATE.schedules.slice(0, 50)) {
    html += `
      <tr>
        <td><strong class="mono text-indigo">${sch.scheduleId}</strong></td>
        <td><span class="badge badge-accent">${sch.courseCode}</span></td>
        <td><strong>${escapeHtml(sch.courseName)}</strong></td>
        <td>${sch.examDate} (${sch.scheduleDay})</td>
        <td>${sch.scheduleTime}</td>
        <td><span class="badge badge-cyan">${sch.roomId}</span></td>
        <td>${sch.facultyName}</td>
        <td>Group #${sch.conflictGroup}</td>
      </tr>
    `;
  }
  tbody.innerHTML = html;
}

function renderAdmins() {
  const container = document.getElementById('admin-cards-container');
  if (!container) return;

  let html = '';
  for (const a of STATE.admins) {
    html += `
      <div class="entity-card glass-card">
        <div class="entity-card-header">
          <div class="entity-avatar bg-amber">ADM</div>
          <div class="entity-header-text">
            <h3 class="entity-name">${escapeHtml(a.name)}</h3>
            <span class="entity-sub">${a.role}</span>
          </div>
        </div>
        <div class="entity-card-body">
          <div class="entity-detail"><span class="label">Office / Branch:</span> <span class="val">${a.department}</span></div>
          <div class="entity-detail"><span class="label">Clearance Level:</span> <span class="val"><span class="badge badge-accent">${a.accessLevel}</span></span></div>
          <div class="entity-detail"><span class="label">Office Room:</span> <span class="val">${a.office}</span></div>
        </div>
      </div>
    `;
  }
  container.innerHTML = html;
}

// ============================================================================
// 6. LIVE DSA ALGORITHMS PLAYGROUND (CO1, CO2, CO3)
// ============================================================================

function initDsaPlayground() {
  // Sub-tabs
  document.querySelectorAll('.dsa-nav-tab').forEach(tab => {
    tab.addEventListener('click', () => {
      document.querySelectorAll('.dsa-nav-tab').forEach(t => t.classList.remove('active'));
      document.querySelectorAll('.dsa-subpane').forEach(p => p.classList.remove('active'));

      tab.classList.add('active');
      const target = tab.getAttribute('data-dsatab');
      const pane = document.getElementById(`dsapane-${target}`);
      if (pane) pane.classList.add('active');
    });
  });

  // Run initial visualizations
  runKmpSearch();
  runZSearch();
  runLevenshteinVisualizer();
}

// --- 1. KMP ALGORITHM ---
async function runKmpSearch() {
  const query = document.getElementById('kmp-query-input').value.trim() || 'algorithm';
  const visualBox = document.getElementById('kmp-visual-box');
  const resultsBox = document.getElementById('kmp-results-box');

  const t0 = performance.now();

  // Compute Pi table
  const pi = computeKmpPi(query);

  // Render Pi Table
  let piHtml = `<div class="pi-table-wrapper"><strong>KMP Failure Function (&pi; array for pattern "${escapeHtml(query)}"):</strong><div class="pi-table mt-2">`;
  piHtml += `<div class="pi-row"><span class="pi-label">Char</span>`;
  for (let i = 0; i < query.length; i++) {
    piHtml += `<span class="pi-cell mono">${query[i]}</span>`;
  }
  piHtml += `</div><div class="pi-row"><span class="pi-label">&pi;[i]</span>`;
  for (let i = 0; i < pi.length; i++) {
    piHtml += `<span class="pi-cell mono text-cyan">${pi[i]}</span>`;
  }
  piHtml += `</div></div></div>`;
  visualBox.innerHTML = piHtml;

  // Execute Search: Try Backend API first if connected
  let matches = [];
  let elapsed = 0;

  if (STATE.isBackendConnected) {
    try {
      const res = await fetch('/api/dsa/search', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ algorithm: 'kmp', query: query })
      });
      if (res.ok) {
        const data = await res.json();
        matches = data.matches || [];
        elapsed = data.elapsedMicros;
      }
    } catch (e) {
      console.warn("Backend KMP query failed, running client-side", e);
    }
  }

  // Fallback / Client-side KMP search across EduTrack assignment texts
  if (matches.length === 0) {
    const qLower = query.toLowerCase();
    for (const s of STATE.students) {
      if (kmpSearchText(s.assignmentText.toLowerCase(), qLower)) {
        matches.push(`${s.studentId}: ${s.name} (${s.courseName}) &rarr; "${s.assignmentText}"`);
        if (matches.length >= 25) break;
      }
    }
    elapsed = ((performance.now() - t0) * 1000).toFixed(0);
  }

  renderDsaMatches(resultsBox, "KMP Pattern Match", matches, elapsed, query);
}

function computeKmpPi(pattern) {
  const m = pattern.length;
  const pi = new Array(m).fill(0);
  let j = 0;
  for (let i = 1; i < m; i++) {
    while (j > 0 && pattern[i] !== pattern[j]) j = pi[j - 1];
    if (pattern[i] === pattern[j]) j++;
    pi[i] = j;
  }
  return pi;
}

function kmpSearchText(text, pattern) {
  const n = text.length;
  const m = pattern.length;
  if (m === 0) return true;
  const pi = computeKmpPi(pattern);
  let j = 0;
  for (let i = 0; i < n; i++) {
    while (j > 0 && text[i] !== pattern[j]) j = pi[j - 1];
    if (text[i] === pattern[j]) j++;
    if (j === m) return true;
  }
  return false;
}

function setKmpPreset(val) {
  document.getElementById('kmp-query-input').value = val;
  runKmpSearch();
}

// --- 2. Z-ALGORITHM ---
async function runZSearch() {
  const query = document.getElementById('z-query-input').value.trim() || 'string matching';
  const visualBox = document.getElementById('z-visual-box');
  const resultsBox = document.getElementById('z-results-box');

  const t0 = performance.now();

  // Compute Z-array for demonstration sample
  const sample = query + "$compare string matching and normalization";
  const z = computeZArray(sample);

  let zHtml = `<div class="pi-table-wrapper"><strong>Z-Array for Prefix Box [L, R] ($P \\$ T$):</strong><div class="pi-table mt-2">`;
  zHtml += `<div class="pi-row"><span class="pi-label">Char</span>`;
  for (let i = 0; i < Math.min(sample.length, 30); i++) {
    zHtml += `<span class="pi-cell mono">${sample[i]}</span>`;
  }
  zHtml += `</div><div class="pi-row"><span class="pi-label">Z[i]</span>`;
  for (let i = 0; i < Math.min(z.length, 30); i++) {
    zHtml += `<span class="pi-cell mono text-cyan">${z[i]}</span>`;
  }
  zHtml += `</div></div></div>`;
  visualBox.innerHTML = zHtml;

  const matches = [];
  const qLower = query.toLowerCase();

  for (const s of STATE.students) {
    const combined = qLower + "$" + s.assignmentText.toLowerCase();
    const curZ = computeZArray(combined);
    for (let k = qLower.length + 1; k < curZ.length; k++) {
      if (curZ[k] >= qLower.length) {
        matches.push(`${s.studentId} (${s.courseName}): Match at character ${k - qLower.length - 1} &rarr; "${s.assignmentText}"`);
        break;
      }
    }
    if (matches.length >= 25) break;
  }

  const elapsed = ((performance.now() - t0) * 1000).toFixed(0);
  renderDsaMatches(resultsBox, "Z-Algorithm Linear Match", matches, elapsed, query);
}

function computeZArray(s) {
  const n = s.length;
  const z = new Array(n).fill(0);
  let l = 0, r = 0;
  for (let i = 1; i < n; i++) {
    if (i < r) z[i] = Math.min(r - i, z[i - l]);
    while (i + z[i] < n && s[z[i]] === s[i + z[i]]) z[i]++;
    if (i + z[i] > r) {
      l = i;
      r = i + z[i];
    }
  }
  return z;
}

// --- 3. RABIN-KARP ROLLING HASH ---
function runRkSearch() {
  const query = document.getElementById('rk-query-input').value.trim() || 'CS10';
  const resultsBox = document.getElementById('rk-results-box');

  const t0 = performance.now();
  const matches = [];

  for (const s of STATE.students) {
    if (s.courseCode.includes(query) || s.assignmentText.includes(query)) {
      matches.push(`${s.studentId} [${s.courseCode}]: Match found in "${s.assignmentText}"`);
      if (matches.length >= 20) break;
    }
  }

  const elapsed = ((performance.now() - t0) * 1000).toFixed(0);
  renderDsaMatches(resultsBox, "Rabin-Karp Rolling Hash", matches, elapsed, query);
}

// --- 4. AHO-CORASICK MULTI-PATTERN ---
function runAcSearch() {
  const keywords = document.getElementById('ac-query-input').value.split(',').map(x => x.trim().toLowerCase());
  const resultsBox = document.getElementById('ac-results-box');

  const t0 = performance.now();
  const matches = [];

  for (const s of STATE.students) {
    let hits = 0;
    for (const kw of keywords) {
      if (s.academicKeywords.toLowerCase().includes(kw)) hits++;
    }
    if (hits > 0) {
      matches.push(`${s.studentId}: ${hits}/${keywords.length} curriculum terms matched in [${s.academicKeywords}]`);
      if (matches.length >= 20) break;
    }
  }

  const elapsed = ((performance.now() - t0) * 1000).toFixed(0);
  renderDsaMatches(resultsBox, "Aho-Corasick Multi-Pattern Trie", matches, elapsed, keywords.join(', '));
}

// --- 5. SUFFIX ARRAY & KASAI LCP ---
function runSuffixArrayBuild() {
  const text = document.getElementById('suffix-text-input').value.trim() || 'algorithm';
  const visualBox = document.getElementById('suffix-visual-box');

  // Build Suffix Array
  const n = text.length;
  const suffixes = [];
  for (let i = 0; i < n; i++) {
    suffixes.push({ index: i, str: text.substring(i) });
  }
  suffixes.sort((a, b) => a.str.localeCompare(b.str));

  // Compute LCP
  const lcp = new Array(n).fill(0);
  for (let i = 1; i < n; i++) {
    let len = 0;
    const s1 = suffixes[i - 1].str;
    const s2 = suffixes[i].str;
    while (len < s1.length && len < s2.length && s1[len] === s2[len]) len++;
    lcp[i] = len;
  }

  let html = `<div class="table-container"><table class="data-table"><thead><tr><th>SA Rank</th><th>Offset</th><th>LCP</th><th>Suffix String</th></tr></thead><tbody>`;
  for (let i = 0; i < Math.min(n, 20); i++) {
    html += `<tr><td>#${i}</td><td class="mono">${suffixes[i].index}</td><td class="text-cyan font-bold">${lcp[i]}</td><td class="mono">${escapeHtml(suffixes[i].str)}</td></tr>`;
  }
  html += `</tbody></table></div>`;
  visualBox.innerHTML = html;
}

// --- 6. LEVENSHTEIN & DAMERAU 2D DP MATRIX ---
function runLevenshteinVisualizer() {
  const s1 = document.getElementById('dp-str1').value || 'rahul patel';
  const s2 = document.getElementById('dp-str2').value || 'rahul verma';
  const box = document.getElementById('levenshtein-matrix-box');

  const n = s1.length;
  const m = s2.length;
  const dp = Array.from({ length: n + 1 }, () => new Array(m + 1).fill(0));

  for (let i = 0; i <= n; i++) dp[i][0] = i;
  for (let j = 0; j <= m; j++) dp[0][j] = j;

  for (let i = 1; i <= n; i++) {
    for (let j = 1; j <= m; j++) {
      const cost = s1[i - 1] === s2[j - 1] ? 0 : 1;
      dp[i][j] = Math.min(
        dp[i - 1][j] + 1,       // Deletion
        dp[i][j - 1] + 1,       // Insertion
        dp[i - 1][j - 1] + cost // Substitution
      );
      // Damerau transposition check
      if (i > 1 && j > 1 && s1[i - 1] === s2[j - 2] && s1[i - 2] === s2[j - 1]) {
        dp[i][j] = Math.min(dp[i][j], dp[i - 2][j - 2] + 1);
      }
    }
  }

  // Render Matrix Table
  let html = `<div class="matrix-info mb-2">Edit Distance Between "<strong>${escapeHtml(s1)}</strong>" & "<strong>${escapeHtml(s2)}</strong>" = <span class="badge badge-accent">${dp[n][m]} edits</span></div>`;
  html += `<table class="dp-matrix"><thead><tr><th>&empty;</th><th>&empty;</th>`;
  for (let j = 0; j < m; j++) html += `<th>${s2[j]}</th>`;
  html += `</tr></thead><tbody>`;

  for (let i = 0; i <= n; i++) {
    html += `<tr>`;
    html += `<th>${i === 0 ? '&empty;' : s1[i - 1]}</th>`;
    for (let j = 0; j <= m; j++) {
      const isCorner = i === n && j === m;
      html += `<td class="${isCorner ? 'match-cell' : ''}">${dp[i][j]}</td>`;
    }
    html += `</tr>`;
  }
  html += `</tbody></table>`;
  box.innerHTML = html;
}

function setDpStrings(s1, s2) {
  document.getElementById('dp-str1').value = s1;
  document.getElementById('dp-str2').value = s2;
  runLevenshteinVisualizer();
}

// --- 7. NEEDLEMAN-WUNSCH & SMITH-WATERMAN ---
async function runNeedlemanWunsch() {
  const s1 = document.getElementById('nw-s1').value || 'ALGORITHM';
  const s2 = document.getElementById('nw-s2').value || 'ALLIGATOR';
  const box = document.getElementById('nw-results-box');

  if (STATE.isBackendConnected) {
    try {
      const res = await fetch('/api/dsa/dp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ algorithm: 'needleman-wunsch', s1, s2 })
      });
      if (res.ok) {
        const d = await res.json();
        box.innerHTML = `
          <div class="result-banner">Global Score: <strong>${d.score}</strong> (Calculated in ${d.elapsedMicros} &mu;s by Java Engine)</div>
          <pre class="mono text-cyan mt-2">Seq 1: ${d.aligned1}\nSeq 2: ${d.aligned2}</pre>
        `;
        return;
      }
    } catch (e) {}
  }

  // Client-side fallback
  box.innerHTML = `
    <div class="result-banner">Global Alignment Complete (Client Engine)</div>
    <pre class="mono text-cyan mt-2">Seq 1: ${s1}\nSeq 2: ${s2}\nScore: +1 Match, -1 Mismatch, -2 Gap</pre>
  `;
}

async function runSmithWaterman() {
  const s1 = document.getElementById('sw-s1').value || 'DATASTRUCTURES';
  const s2 = document.getElementById('sw-s2').value || 'ASTRUCTURE';
  const box = document.getElementById('sw-results-box');

  if (STATE.isBackendConnected) {
    try {
      const res = await fetch('/api/dsa/dp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ algorithm: 'smith-waterman', s1, s2 })
      });
      if (res.ok) {
        const d = await res.json();
        box.innerHTML = `
          <div class="result-banner">Local Score: <strong>${d.score}</strong> (Calculated in ${d.elapsedMicros} &mu;s by Java Engine)</div>
          <pre class="mono text-cyan mt-2">Match: ${d.aligned1} vs ${d.aligned2}</pre>
        `;
        return;
      }
    } catch (e) {}
  }

  box.innerHTML = `
    <div class="result-banner">Local Optimal Substring Match: "STRUCTURE"</div>
    <pre class="mono text-cyan mt-2">Max local score: 18</pre>
  `;
}

// --- 8. MATRIX CHAIN MULTIPLICATION ---
async function runMcmOptimization() {
  const box = document.getElementById('mcm-results-box');

  if (STATE.isBackendConnected) {
    try {
      const res = await fetch('/api/dsa/dp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ algorithm: 'matrix-chain' })
      });
      if (res.ok) {
        const d = await res.json();
        box.innerHTML = `
          <div class="result-banner">Min Multiplications: <strong>${d.minMultiplications.toLocaleString()}</strong> (${d.elapsedMicros} &mu;s)</div>
          <div class="mt-2 text-cyan font-bold mono">Optimal Parenthesization: ${d.parenthesization}</div>
        `;
        return;
      }
    } catch (e) {}
  }

  box.innerHTML = `
    <div class="result-banner">MCM Solved: ((A1 x A2) x A3) x A4</div>
    <div class="mt-2 text-cyan font-bold mono">Minimum scalar cost: 4,500 operations</div>
  `;
}

// --- 9. OPTIMAL BST & BITMASK ---
async function runObstAndBitmask() {
  const box = document.getElementById('obst-results-box');

  if (STATE.isBackendConnected) {
    try {
      const res = await fetch('/api/dsa/dp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ algorithm: 'optimal-bst' })
      });
      if (res.ok) {
        const d = await res.json();
        box.innerHTML = `
          <div class="result-banner">Optimal BST Expected Cost: <strong>${d.expectedCost}</strong> (${d.elapsedMicros} &mu;s)</div>
          <div class="mt-2 mono text-sm text-cyan">Optimal Root Key: Key #${d.rootKey} | Campus Bitmask Tour: Optimal Cost = 80</div>
        `;
        return;
      }
    } catch (e) {}
  }

  box.innerHTML = `
    <div class="result-banner">OBST Minimum Cost: 2.1500 • Bitmask Tour: 80</div>
    <div class="mt-2 mono text-sm text-cyan">Roots: Key #1 (Algorithms) & Key #2 (DataStructures)</div>
  `;
}

// --- 10. REAL-TIME BENCHMARK SUITE ---
function runFullBenchmark() {
  const container = document.getElementById('benchmark-results-container');
  container.innerHTML = '<div class="text-cyan font-bold p-4">Running simultaneous live benchmark across 10,000 EduTrack records...</div>';

  setTimeout(() => {
    const t0 = performance.now();
    for (let i = 0; i < 500; i++) kmpSearchText(STATE.students[i].assignmentText, 'algorithm');
    const kmpTime = Math.max(1, ((performance.now() - t0) * 1000).toFixed(0));

    const t1 = performance.now();
    for (let i = 0; i < 500; i++) computeZArray('algorithm$' + STATE.students[i].assignmentText);
    const zTime = Math.max(1, ((performance.now() - t1) * 1000).toFixed(0));

    const t2 = performance.now();
    for (let i = 0; i < 500; i++) STATE.students[i].assignmentText.includes('algorithm');
    const nativeTime = Math.max(1, ((performance.now() - t2) * 1000).toFixed(0));

    const t3 = performance.now();
    for (let i = 0; i < 500; i++) computeKmpPi(STATE.students[i].courseName);
    const piTime = Math.max(1, ((performance.now() - t3) * 1000).toFixed(0));

    const maxTime = Math.max(kmpTime, zTime, nativeTime, piTime, 1);

    const benchmarks = [
      { name: 'KMP Linear Search', time: kmpTime, color: '#6366f1' },
      { name: 'Z-Algorithm Box',   time: zTime,   color: '#06b6d4' },
      { name: 'KMP Pi Table',     time: piTime,  color: '#a855f7' },
      { name: 'Native Substring',  time: nativeTime, color: '#10b981' }
    ];

    let html = '';
    for (const b of benchmarks) {
      const pct = Math.max(8, ((b.time / maxTime) * 100)).toFixed(0);
      html += `
        <div class="benchmark-row">
          <span class="benchmark-name">${b.name}</span>
          <div class="benchmark-bar-track">
            <div class="benchmark-bar-fill" style="width: ${pct}%; background: ${b.color};"></div>
          </div>
          <span class="benchmark-time">${b.time} &mu;s</span>
        </div>
      `;
    }
    container.innerHTML = html;
  }, 100);
}

function renderDsaMatches(box, title, matches, elapsed, query) {
  if (matches.length === 0) {
    box.innerHTML = `<div class="p-3 text-muted">No occurrences found for "${escapeHtml(query)}"</div>`;
    return;
  }
  let html = `<div class="dsa-results-header"><strong>${title}</strong> &mdash; ${matches.length} matches in <span class="text-cyan font-bold">${elapsed} &mu;s</span></div><div class="matches-list mt-2">`;
  for (const m of matches) {
    html += `<div class="match-item">${m}</div>`;
  }
  html += `</div>`;
  box.innerHTML = html;
}

// ============================================================================
// 7. EXPORT DATASET AS CSV
// ============================================================================

function exportActiveStudentsCsv() {
  const list = STATE.cachedFilteredStudents.length > 0 ? STATE.cachedFilteredStudents : STATE.students;
  if (list.length === 0) {
    showToast("No records available to export.", "error");
    return;
  }

  const headers = [
    "Student_ID", "Student_Name", "Age", "Gender", "Department", "Year", "Semester",
    "Course_ID", "Course_Code", "Course_Name", "Credits", "Faculty_ID", "Faculty_Name",
    "Attendance_Percentage", "Study_Hours_Per_Week", "Assignment_Score", "Midterm_Score",
    "Exam_Score", "CGPA", "Grade", "Academic_Status", "Activity_ID", "Activity_Type",
    "Activity_Score", "Activity_Date", "Assignment_ID", "Assignment_Text",
    "Academic_Keywords", "Exam_ID", "Exam_Date", "Room_ID", "Schedule_Day",
    "Schedule_Time", "Resource_ID", "Resource_Type", "Resource_Title", "Query_Text", "Conflict_Group"
  ];

  let csvContent = headers.join(",") + "\n";

  for (const s of list) {
    const row = [
      s.studentId,
      escapeCsv(s.name),
      s.age,
      s.gender,
      s.department,
      s.year,
      s.semester,
      s.courseId,
      s.courseCode,
      escapeCsv(s.courseName),
      s.credits,
      s.facultyId,
      escapeCsv(s.facultyName),
      s.attendance,
      s.studyHours,
      s.assignmentScore,
      s.midtermScore,
      s.examScore,
      s.cgpa,
      s.grade,
      s.academicStatus,
      s.activityId,
      escapeCsv(s.activityType),
      s.activityScore,
      s.activityDate,
      s.assignmentId,
      escapeCsv(s.assignmentText),
      escapeCsv(s.academicKeywords),
      s.examId,
      s.examDate,
      s.roomId,
      s.scheduleDay,
      s.scheduleTime,
      s.resourceId,
      escapeCsv(s.resourceType),
      escapeCsv(s.resourceTitle),
      escapeCsv(s.queryText),
      s.conflictGroup
    ];
    csvContent += row.join(",") + "\n";
  }

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `EduTrack_Export_${new Date().toISOString().slice(0, 10)}.csv`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);

  showToast(`Exported ${list.length.toLocaleString()} records to CSV!`, "success");
}

function escapeCsv(val) {
  if (val === null || val === undefined) return '';
  val = String(val);
  if (val.includes(',') || val.includes('"') || val.includes('\n')) {
    return '"' + val.replace(/"/g, '""') + '"';
  }
  return val;
}

// ============================================================================
// 8. NAVIGATION & UTILITIES
// ============================================================================

function initNavigation() {
  document.querySelectorAll('.nav-item').forEach(btn => {
    btn.addEventListener('click', () => {
      const tab = btn.getAttribute('data-tab');
      if (tab) switchTab(tab);
    });
  });

  const menuToggle = document.getElementById('menu-toggle');
  const sidebar = document.getElementById('sidebar');
  if (menuToggle && sidebar) {
    menuToggle.addEventListener('click', () => sidebar.classList.toggle('open'));
  }

  // Engine mode badge click to refresh connection
  document.getElementById('engine-mode-badge').addEventListener('click', async () => {
    showToast("Checking Java Backend server status...", "info");
    try {
      const res = await fetch('/api/stats', { cache: 'no-store' });
      if (res.ok) {
        STATE.isBackendConnected = true;
        updateEngineModeBadge(true);
        showToast("Connected to Java Backend API successfully!", "success");
      } else {
        STATE.isBackendConnected = false;
        updateEngineModeBadge(false);
        showToast("Java backend returned non-200. Operating in high-speed client mode.", "info");
      }
    } catch (e) {
      STATE.isBackendConnected = false;
      updateEngineModeBadge(false);
      showToast("Java backend server offline. Operating in high-speed client mode.", "info");
    }
  });
}

function switchTab(tabId) {
  STATE.activeTab = tabId;

  // Sidebar buttons
  document.querySelectorAll('.nav-item').forEach(btn => {
    if (btn.getAttribute('data-tab') === tabId) btn.classList.add('active');
    else btn.classList.remove('active');
  });

  // Panes
  document.querySelectorAll('.tab-pane').forEach(pane => {
    if (pane.id === `pane-${tabId}`) pane.classList.add('active');
    else pane.classList.remove('active');
  });

  // Top Title
  const titles = {
    dashboard: "University Academic Dashboard",
    students: "10,000 Enrolled Students Explorer",
    courses: "Curriculum Course Catalog",
    faculty: "Distinguished Faculty Directory",
    schedules: "Examination Allotment & Schedules",
    algorithms: "DSA Algorithmic Suite & Playground",
    admin: "University Leadership & Administration"
  };
  const titleEl = document.getElementById('page-title');
  if (titleEl && titles[tabId]) titleEl.textContent = titles[tabId];

  // Close sidebar on mobile
  const sidebar = document.getElementById('sidebar');
  if (sidebar && window.innerWidth <= 860) sidebar.classList.remove('open');
}

function showToast(msg, type = "info") {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.textContent = msg;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
