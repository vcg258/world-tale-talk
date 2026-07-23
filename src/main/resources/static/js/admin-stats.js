(function () {
  const periodSelect = document.querySelector('[data-period-select]');
  const customFields = document.querySelectorAll('[data-custom-period]');

  const syncCustomFields = () => {
    const isCustom = periodSelect && periodSelect.value === 'custom';
    customFields.forEach((field) => {
      field.hidden = !isCustom;
    });
  };

  if (periodSelect) {
    periodSelect.addEventListener('change', syncCustomFields);
    syncCustomFields();
  }

  if (!window.Chart) {
    return;
  }

  const statsData = window.adminStatsData || {};
  const readData = (name) => statsData[name] || [];

  const palette = [
    '#2563eb', '#f59e0b', '#10b981', '#ef4444', '#8b5cf6', '#14b8a6',
    '#f97316', '#64748b', '#db2777', '#0891b2', '#84cc16', '#9333ea'
  ];

  const colorForIndex = (index) => palette[index % palette.length];

  const hashColor = (label) => {
    const text = String(label || '기타');
    let hash = 0;
    for (let i = 0; i < text.length; i += 1) {
      hash = ((hash << 5) - hash) + text.charCodeAt(i);
      hash |= 0;
    }
    return palette[Math.abs(hash) % palette.length];
  };

  const makeWorldColorMap = (labels) => {
    const map = new Map();
    labels.forEach((label, index) => {
      map.set(label, colorForIndex(index));
    });
    return map;
  };

  const renderDoughnut = (id, labels, values, colors) => {
    const canvas = document.getElementById(id);
    if (!canvas) return;
    new Chart(canvas, {
      type: 'doughnut',
      data: {
        labels,
        datasets: [{
          data: values,
          backgroundColor: colors,
          borderWidth: 0,
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {position: 'bottom'},
        },
      },
    });
  };

  const renderBar = (id, labels, values, colors) => {
    const canvas = document.getElementById(id);
    if (!canvas) return;
    new Chart(canvas, {
      type: 'bar',
      data: {
        labels,
        datasets: [{
          label: '건수',
          data: values,
          backgroundColor: colors,
          borderRadius: 6,
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: {beginAtZero: true, ticks: {precision: 0}},
        },
        plugins: {
          legend: {display: false},
        },
      },
    });
  };

  const renderTrend = (id, labels, totalCounts, endingCounts, abandonedCounts, activeCounts) => {
    const canvas = document.getElementById(id);
    if (!canvas) return;
    new Chart(canvas, {
      type: 'bar',
      data: {
        labels,
        datasets: [
          {label: '이야기 수', data: totalCounts, backgroundColor: 'rgba(37, 99, 235, 0.72)', borderColor: '#2563eb', borderWidth: 1, borderRadius: 6},
          {label: '엔딩', data: endingCounts, backgroundColor: 'rgba(16, 185, 129, 0.72)', borderColor: '#10b981', borderWidth: 1, borderRadius: 6},
          {label: '포기', data: abandonedCounts, backgroundColor: 'rgba(239, 68, 68, 0.72)', borderColor: '#ef4444', borderWidth: 1, borderRadius: 6},
          {label: '진행중', data: activeCounts, backgroundColor: 'rgba(245, 158, 11, 0.72)', borderColor: '#f59e0b', borderWidth: 1, borderRadius: 6},
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {mode: 'index', intersect: false},
        datasets: {
          bar: {categoryPercentage: 0.72, barPercentage: 0.82},
        },
        scales: {
          y: {beginAtZero: true, ticks: {precision: 0}},
        },
        plugins: {
          legend: {position: 'bottom'},
        },
      },
    });
  };

  const statusLabels = readData('statusLabels');
  const statusCounts = readData('statusCounts');
  const worldLabels = readData('worldLabels');
  const worldCounts = readData('worldCounts');
  const personaLabels = readData('personaLabels');
  const personaCounts = readData('personaCounts');
  const personaGroups = readData('personaGroups');
  const worldColorMap = makeWorldColorMap(worldLabels);
  const worldColors = worldLabels.map((label) => worldColorMap.get(label) || hashColor(label));
  const personaColors = personaGroups.map((group) => worldColorMap.get(group) || hashColor(group));

  renderTrend(
    'trendChart',
    readData('timeLabels'),
    readData('timeTotalCounts'),
    readData('timeEndingCounts'),
    readData('timeAbandonedCounts'),
    readData('timeActiveCounts')
  );
  renderDoughnut('statusChart', statusLabels, statusCounts, ['#10b981', '#ef4444', '#f59e0b']);
  renderBar('worldChart', worldLabels, worldCounts, worldColors);
  renderBar('personaChart', personaLabels, personaCounts, personaColors);
})();
