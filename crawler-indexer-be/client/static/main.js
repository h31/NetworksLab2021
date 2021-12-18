const searchInput = document.getElementById('search-input');
const resHolder = document.getElementById('res-holder');
const settingsInputs = document.getElementById('settings-inputs');
const hostInput = document.getElementById('host-input');
const portInput = document.getElementById('port-input');
const searchBtn = document.getElementById('search-btn');


const interactive = [
  searchInput,
  hostInput,
  portInput,
  searchBtn
];

function makeCard(res) {
  const card = document.createElement('div');
  card.classList.add('card');

  const link = document.createElement('a');
  link.href = res.url;
  link.innerText = res.title;
  link.rel = 'noopener noreferrer';
  link.target = '_blank';
  card.appendChild(link);

  if (res.preview.length) {
    const openPreviewBtn = document.createElement('button');
    openPreviewBtn.textContent = 'Preview';
    openPreviewBtn.onclick = () => {
      card.querySelectorAll('.preview-block').forEach(el => el.classList.toggle('no-display'));
    };
    card.appendChild(openPreviewBtn);

    res.preview.forEach(blockText => {
      const block = document.createElement('p');
      block.textContent = `...${blockText.replace(/\n/g, ' > ').trim()}...`;
      block.classList.add('preview-block', 'no-display')
      card.appendChild(block);
    });
  }

  return card;
}

async function performRequest() {
  const searching = document.createElement('span');
  searching.textContent = 'Searching...';
  resHolder.innerHTML = '';
  resHolder.appendChild(searching);
  interactive.forEach(node => node.setAttribute('disabled', 'true'));
  try {
    const response = await fetch(`http://${hostInput.value}:${portInput.value}/api/search?q=${searchInput.value}`);
    if (response.ok) {
      const results = await response.json();
      resHolder.append(...results.map(makeCard));
    } else {
      resHolder.innerHTML = `<h1>${response.status}: ${response.statusText}</h1>`;
    }
  } catch (e) {
    resHolder.innerHTML = `<h1>${e.message}</h1>`;
  } finally {
    searching.remove();
    interactive.forEach(node => node.removeAttribute('disabled'));
  }
}

function toggleSettingsVisibility() {
  settingsInputs.classList.toggle('hidden');
}
