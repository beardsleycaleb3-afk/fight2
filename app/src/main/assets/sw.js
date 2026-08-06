const CACHE_NAME = 'fight-engine-v1';
const ASSETS_TO_CACHE = [
  './',
  './index.html',
  './manifest.json',
  'fight/assets/sprites/fighter/east/idle/',
  'fight/assets/sprites/fighter/east/walk/',
  'fight/assets/sprites/fighter/east/run/',
  'fight/assets/sprites/fighter/east/jump/',
  'fight/assets/sprites/fighter/east/jab/',
  'fight/assets/sprites/fighter/east/cross/',
  'fight/assets/sprites/fighter/east/punch/',
  'fight/assets/sprites/fighter/east/kick/',
  'fight/assets/sprites/fighter/east/roundhouse/',
  'fight/assets/sprites/fighter/east/headbutt/',
  'fight/assets/sprites/fighter/east/uppercut/',
  'fight/assets/sprites/fighter/east/victory/',
  'fight/assets/sprites/fighter/flaming/east/cross/',
  'fight/assets/sprites/fighter/flaming/east/headbutt/',
  'fight/assets/sprites/fighter/flaming/east/idle/',
  'fight/assets/sprites/fighter/flaming/east/jab/',
  'fight/assets/sprites/fighter/flaming/east/jump/',
  'fight/assets/sprites/fighter/flaming/east/kick/',
  'fight/assets/sprites/fighter/flaming/east/punch/',
  'fight/assets/sprites/fighter/flaming/east/roundhouse/',
  'fight/assets/sprites/fighter/flaming/east/run/',
  'fight/assets/sprites/fighter/flaming/east/uppercut/',
  'fight/assets/sprites/fighter/flaming/east/victory/',
  'fight/assets/sprites/fighter/flaming/east/walk/'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      console.log('Opened cache and storing fighter sprite assets...');
      return cache.addAll(ASSETS_TO_CACHE).catch(err => console.warn('Cache prefetch soft notice:', err));
    })
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cache) => {
          if (cache !== CACHE_NAME) {
            console.log('Clearing old cache:', cache);
            return caches.delete(cache);
          }
        })
      );
    })
  );
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  event.respondWith(
    caches.match(event.request).then((response) => {
      return response || fetch(event.request);
    })
  );
});
