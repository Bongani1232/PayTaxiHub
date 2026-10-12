const API_BASE_URL = (location.protocol === 'http:' || location.protocol === 'https:') ? `${location.origin}/api` : '/api';

function $(id){ return document.getElementById(id); }
function setText(id,value){ const el=$(id); if(el) el.textContent=String(value ?? ''); }
function byId(id){ return document.getElementById(id); }
function qs(name){ return new URLSearchParams(location.search).get(name); }
function buildShiftLoginUrl(code){
  const url=new URL('login.html',location.href);
  url.searchParams.set('role','Commuter');
  url.searchParams.set('shiftCode',String(code||'').trim());
  return url.toString();
}
function shiftQrNetworkNotice(){
  const host=location.hostname.toLowerCase();
  return host==='localhost'||host.endsWith('.localhost')||host==='::1'||host==='[::1]'||/^127(?:\.\d{1,3}){3}$/.test(host)
    ? 'This QR uses localhost, which only works on this computer. Open PayTaxiHub using your computer’s network address on the same Wi-Fi, or deploy it to a public HTTPS address before scanning.'
    : '';
}
function showShiftQrNetworkNotice(id,afterId){
  const message=shiftQrNetworkNotice();
  let notice=byId(id);
  if(!notice&&message){
    notice=document.createElement('p');
    notice.id=id;
    notice.className='text-sm text-amber-700 mt-3';
    notice.setAttribute('role','status');
    byId(afterId).after(notice);
  }
  if(notice){notice.textContent=message;notice.classList.toggle('hidden',!message);}
}
function esc(value){ return escapeHtml(value); }
function escapeHtml(value){ return String(value ?? '').replace(/[&<>'"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[c])); }
function money(v){ return `R ${Number(v || 0).toFixed(2)}`; }
function fmtDate(v){ if(!v)return '—'; const d=new Date(v); return Number.isNaN(d.getTime())?String(v):d.toLocaleString('en-ZA',{dateStyle:'medium',timeStyle:'short'}); }
function toast(message,type=''){ const box=$('toast-container'); if(!box){ console[type==='error'?'error':'log'](message); return; } const el=document.createElement('div'); el.className=`toast ${type}`; el.textContent=message; box.appendChild(el); setTimeout(()=>el.remove(),4500); }
function setRoleButtons(role){ document.querySelectorAll('.role-btn').forEach(b=>b.classList.toggle('active',b.dataset.role===role)); if($('role'))$('role').value=role; }
function normalizeRole(v){ const x=String(v||'').trim(); const key=x.toLowerCase(); if(key==='passenger'||key==='commuter')return 'Commuter'; if(key==='driver')return 'Driver'; if(key==='owner'||key==='fleetowner'||key==='fleet owner')return 'FleetOwner'; if(key==='admin'||key==='administrator')return 'Admin'; return x||'Commuter'; }
function setSession(user,token){
  if(!user)return null;
  const identity={userId:user.userId,role:user.role,fullName:user.fullName};
  sessionStorage.setItem('pth_session',JSON.stringify(identity));
  localStorage.removeItem('pth_session');
  localStorage.removeItem('pth_token');
  if(token)sessionStorage.setItem('pth_token',token);
  return identity;
}
function setUser(user,token){ return setSession(user,token); }
function getSession(){ try{return JSON.parse(sessionStorage.getItem('pth_session')||'null');}catch{return null;} }
function getToken(){ return sessionStorage.getItem('pth_token')||''; }
function clearLegacySessionStorage(){
  localStorage.removeItem('pth_session');
  localStorage.removeItem('pth_token');
}
function clearSession(){
  clearLegacySessionStorage();
  sessionStorage.removeItem('pth_session');
  sessionStorage.removeItem('pth_token');
}
function rolePath(role){ return role==='Driver'?'driver_dash.html':role==='FleetOwner'?'owner_dash.html':role==='Admin'?'admin.html':'commuter_dash.html'; }
function goDashboard(role){ location.href=rolePath(role); }
function requireUser(role){
  const u=getSession(), token=getToken();
  if(!u || !token){ location.replace('login.html'); return null; }
  // Keep each role in its own portal. In particular, an Admin must never silently land on the commuter dashboard.
  if(role && u.role!==role){ location.replace(rolePath(u.role)); return null; }
  return u;
}
function guessRoleForPage(){
  const p=location.pathname.toLowerCase();
  if(p.includes('admin')) return 'Admin';
  if(p.includes('fleet')||p.includes('owner_dash')) return 'FleetOwner';
  if(p.includes('driver')||p.includes('vehicle')||p.includes('shift')||p.includes('earnings')||p.includes('gps')) return 'Driver';
  if(p.includes('commuter')||p.includes('wallet')||p.includes('payment')||p.includes('transaction')||p==='/'||p.endsWith('trips.html')) return 'Commuter';
  return null;
}
async function request(path,options={}){
  let clean=String(path).replace(/^\/+/, '');
  if(clean==='sos-events' || clean.startsWith('sos-events/')) clean='sos'+clean.substring('sos-events'.length);
  const headers={'Content-Type':'application/json',...(options.headers||{})};
  const token=getToken(); if(token) headers.Authorization=`Bearer ${token}`;
  let response;
  try{
    response=await fetch(`${API_BASE_URL}/${clean}`,{...options,headers,cache:'no-store'});
  }catch(error){
    throw new Error('Unable to connect right now. Check your connection and try again.',{cause:error});
  }
  const text=await response.text(); let data=null; try{data=text?JSON.parse(text):null;}catch{data=text;}
  // Sign-in / registration / reset calls answer 401 or 4xx for ordinary reasons (wrong password, bad code): show that message, never treat it as an expired session.
  const isAuthCall=/^auth\/(login|register|mfa\/verify|forgot-password|reset-password|logout)$/.test(clean);
  if(response.status===401 && !isAuthCall){ clearSession(); toast('Your session has expired. Please sign in again.','error'); setTimeout(()=>{location.href='login.html';},900); throw new Error('Your session has expired. Please sign in again.'); }
  if(response.status===403) throw new Error(data?.message||'You do not have permission to perform this action.');
  if(!response.ok) throw new Error(data?.message||data?.error||`Request failed (${response.status}). Please try again.`);
  return data;
}
function normalizeList(path,data){
  if(!Array.isArray(data)) return data;
  if(path==='/wallets') return data.map(x=>({...x,user:x.user||{userId:x.userId}}));
  if(path==='/drivers') return data.map(x=>({...x,user:x.user||{userId:x.userId,fullName:x.fullName,email:x.email}}));
  if(path==='/vehicles') return data.map(x=>({...x,driver:x.driver||{driverId:x.driverId,user:{fullName:x.driverName}}}));
  if(path==='/trips') return data.map(x=>({...x,driver:x.driver|| (x.driverId?{driverId:x.driverId,user:{fullName:x.driverName}}:null),commuter:x.commuter||(x.commuterId?{userId:x.commuterId,fullName:x.commuterName}:null),vehicle:x.vehicle||(x.vehicleId?{vehicleId:x.vehicleId,registrationNumber:x.registrationNumber}:null),route:x.route||(x.routeId?{routeId:x.routeId,routeName:x.routeName,startLocation:x.startLocation,destination:x.destination}:null)}));
  if(path==='/sos') return data.map(x=>({...x,user:x.user||{userId:x.userId}}));
  if(path==='/shifts') return data.map(x=>({...x,driver:x.driver||{driverId:x.driverId},vehicle:x.vehicle||{vehicleId:x.vehicleId,registrationNumber:x.registrationNumber}}));
  return data;
}
async function list(path){
  const clean=path==='sos-events'?'/sos':path;
  if(clean==='/shifts'){
    const u=getSession();
    if(u?.role==='Driver'){
      const ds=await list('/drivers'); const d=ds.find(x=>Number(x.userId||x.user?.userId)===Number(u.userId));
      return d?normalizeList('/shifts',await request(`shifts/driver/${d.driverId}`)):[];
    }
    return [];
  }
  return normalizeList(clean,await request(clean));
}
const api=request;
async function loginUser(email,password,role){ return request('auth/login',{method:'POST',body:JSON.stringify({email,password,role})}); }
async function verifySession(){ return request('auth/me'); }
async function registerUser(payload){ return request('auth/register',{method:'POST',body:JSON.stringify(payload)}); }
async function loadWallet(userId){ return request(`wallets/user/${userId}`); }
async function topUpWallet(userId,amount,paymentMethod='Bank Card'){ return request('wallets/topup',{method:'POST',body:JSON.stringify({userId,amount,paymentMethod})}); }
async function loadTripsForCommuter(userId){ return request(`trips/commuter/${userId}`); }
async function loadTripsForDriver(driverId){ return request(`trips/driver/${driverId}`); }
async function loadDrivers(){ return list('/drivers'); }
async function loadVehicles(){ return list('/vehicles'); }
async function loadDriverVehicles(driverId){ return request(`vehicles/driver/${driverId}`); }
async function createVehicle(payload){ return request('vehicles',{method:'POST',body:JSON.stringify(payload)}); }
async function updateVehicle(id,payload){ return request(`vehicles/${id}`,{method:'PUT',body:JSON.stringify(payload)}); }
async function loadFleetOwners(){ return request('fleet-owners'); }
async function loadShifts(driverId){ return request(`shifts/driver/${driverId}`); }
async function createShift(driverId,vehicleId){ return request('shifts',{method:'POST',body:JSON.stringify({driverId,vehicleId})}); }
async function finishShift(id,totalEarnings){ return request(`shifts/${id}/complete`,{method:'PUT',body:JSON.stringify(totalEarnings==null?{}:{totalEarnings})}); }
async function payDriver(userId,driverId,amount){ return request('payments',{method:'POST',body:JSON.stringify({userId,driverId,amount})}); }
async function sendSos(userId,coords={}){ return request('sos',{method:'POST',body:JSON.stringify({userId,...coords})}); }
async function loadRoutes(){ return request('routes'); }
async function createTrip(payload){ return request('trips',{method:'POST',body:JSON.stringify(payload)}); }
async function updateTripLocation(tripId,latitude,longitude){ return request(`trips/${tripId}/location`,{method:'PUT',body:JSON.stringify({latitude,longitude})}); }
async function loadAllTrips(){ return list('/trips'); }
async function loadAllSos(){ return list('/sos'); }
async function loadUserSos(userId){ return request(`sos/user/${userId}`); }
async function logout(){
  const token=getToken();
  clearSession();
  try{
    const response=await fetch(`${API_BASE_URL}/auth/logout`,{
      method:'POST',
      headers:token?{Authorization:`Bearer ${token}`}:{},
      credentials:'same-origin',
      cache:'no-store'
    });
    if(!response.ok)throw new Error('Session revocation was not confirmed.');
  }catch{
    sessionStorage.setItem('pth_logout_warning','true');
  }finally{
    location.replace('login.html');
  }
}
function clearFormValues(form){
  if(!form)return;
  form.querySelectorAll('input:not([type="hidden"]),textarea').forEach(field=>{field.value='';});
  form.querySelectorAll('input[type="checkbox"],input[type="radio"]').forEach(field=>{field.checked=false;});
}
function guardRestoredProtectedPage(event){
  if(!event.persisted)return;
  const role=guessRoleForPage();
  if(!role)return;
  document.documentElement.style.visibility='hidden';
  const user=getSession();
  if(!getToken()||!user||normalizeRole(user.role)!==role){
    clearSession();
    location.replace('login.html');
    return;
  }
  verifySession().then(result=>{
    if(normalizeRole(result?.user?.role)!==role){
      clearSession();
      location.replace('login.html');
      return;
    }
    location.reload();
  }).catch(()=>{
    clearSession();
    location.replace('login.html');
  });
}
window.addEventListener('pagehide',()=>{
  const role=guessRoleForPage();
  if(!role)return;
  document.documentElement.style.visibility='hidden';
  const main=document.querySelector('main');
  if(main)main.replaceChildren();
  document.querySelectorAll('[data-session-name],#who').forEach(element=>{element.textContent='';});
});
window.addEventListener('pageshow',guardRestoredProtectedPage);
function startLiveRefresh(fn, interval=5000){
  let busy=false;
  const tick=async()=>{ if(busy || document.hidden) return; busy=true; try{await fn();}catch(e){console.error(e);}finally{busy=false;} };
  const timer=setInterval(tick,interval);
  window.addEventListener('beforeunload',()=>clearInterval(timer),{once:true});
  return timer;
}
function fillTable(id,rows,colspan){
  const body=$(id); if(!body)return;
  body.innerHTML=rows.length?rows.join(''):`<tr><td class="p-4 text-gray-500" colspan="${colspan||1}">No records found.</td></tr>`;
}
function bindSessionUi(){
  document.querySelectorAll('[data-session-name]').forEach(e=>e.textContent=getSession()?.fullName||'User');
  document.querySelectorAll('[data-year]').forEach(e=>e.textContent=new Date().getFullYear());
  document.querySelectorAll('[data-logout]').forEach(a=>a.addEventListener('click',e=>{e.preventDefault();logout();}));
  const h=document.querySelector('.pth-hamburger'),m=document.querySelector('.pth-mobile-menu');
  if(h&&m){
    const menuId=m.id||'mobile-navigation';
    m.id=menuId;
    h.setAttribute('aria-controls',menuId);
    h.setAttribute('aria-expanded','false');
    h.setAttribute('aria-label','Open menu');
    const setMenuOpen=open=>{
      m.classList.toggle('open',open);
      h.setAttribute('aria-expanded',String(open));
      h.setAttribute('aria-label',open?'Close menu':'Open menu');
    };
    h.addEventListener('click',()=>setMenuOpen(!m.classList.contains('open')));
    m.addEventListener('click',e=>{if(e.target.closest('a'))setMenuOpen(false);});
    document.addEventListener('keydown',e=>{
      if(e.key==='Escape'&&m.classList.contains('open')){
        setMenuOpen(false);
        h.focus();
      }
    });
  }
}
function bindAuthFormPrivacy(){
  const page=(location.pathname.split('/').pop()||'').toLowerCase();
  if(page!=='login.html'&&page!=='register.html')return;
  const form=byId(page==='login.html'?'loginForm':'registerForm');
  if(!form)return;
  form.setAttribute('autocomplete','off');
  if(page==='login.html'){
    byId('email')?.setAttribute('autocomplete','off');
    byId('password')?.setAttribute('autocomplete','new-password');
    byId('mfaCode')?.setAttribute('autocomplete','off');
  }
  const clearFields=()=>{
    clearFormValues(form);
    if(page==='login.html'&&byId('role'))byId('role').value=qs('shiftCode')?'Commuter':'';
  };
  clearFields();
  window.addEventListener('pagehide',clearFields);
  window.addEventListener('pageshow',clearFields);
  const logoutWarning=sessionStorage.getItem('pth_logout_warning');
  if(page==='login.html'&&logoutWarning){
    sessionStorage.removeItem('pth_logout_warning');
    const message=byId('loginError');
    if(message){
      message.className='notice warning';
        message.textContent='The server could not confirm sign-out. This browser cleared its saved credentials, but the server session may remain active until it expires. Keep this device private.';
    }
  }
}
function installGlobalSos(){
  const u=getSession();
  if(!u || !getToken() || u.role==='Admin' || document.getElementById('globalSos')) return;
  const b=document.createElement('button'); b.id='globalSos'; b.type='button';
  b.innerHTML='<i class="fa-solid fa-triangle-exclamation"></i><span>SOS</span>';
  b.setAttribute('aria-label','Send emergency SOS');
  b.style.cssText='position:fixed;right:18px;bottom:18px;z-index:9998;background:#dc2626;color:#fff;border:0;border-radius:999px;padding:13px 18px;display:flex;align-items:center;gap:8px;font-weight:800;box-shadow:0 10px 25px rgba(0,0,0,.22);cursor:pointer;font-size:14px;';
  b.onclick=async()=>{
    if(!confirm('Send an emergency SOS now? Your current GPS position will be attached when available.')) return;
    b.disabled=true; b.style.opacity='.65';
    const send=async(coords={})=>{try{const r=await sendSos(u.userId,coords);toast(`SOS sent${r?.tripId?` for trip #${r.tripId}`:''}. Help has been alerted.`,'success');}catch(e){toast(e.message,'error');}finally{b.disabled=false;b.style.opacity='1';}};
    if(navigator.geolocation){navigator.geolocation.getCurrentPosition(p=>send({latitude:p.coords.latitude,longitude:p.coords.longitude}),()=>send({}),{enableHighAccuracy:true,timeout:6000,maximumAge:5000});}else send({});
  };
  document.body.appendChild(b);
}

document.addEventListener('DOMContentLoaded',()=>{
  clearLegacySessionStorage();
  bindAuthFormPrivacy();
  bindSessionUi();
  installGlobalSos();
  const protectedPage=guessRoleForPage();
  const publicPages=['index.html','login.html','register.html','about.html','contact.html','driver.html','fleet.html'];
  const current=(location.pathname.split('/').pop()||'index.html').toLowerCase();
  if(protectedPage && !publicPages.includes(current)) requireUser(protectedPage);
});

async function loadAvailableDrivers(){ return request('drivers/available'); }
async function verifyDriverQr(code){
  let value=String(code||'').trim();
  try{
    const u=new URL(value,location.href);
    const fromUrl=u.searchParams.get('shiftCode')||u.searchParams.get('code');
    if(fromUrl) value=fromUrl;
  }catch{}
  return request(`drivers/verify?code=${encodeURIComponent(value)}`);
}
async function loadDriverRequests(driverId){ return request(`trips/driver/${driverId}/requests`); }
async function acceptTrip(tripId){ return request(`trips/${tripId}/accept`,{method:'PUT'}); }
async function completeTrip(tripId,distanceKm,fareAmount){ return request(`trips/${tripId}/complete`,{method:'PUT',body:JSON.stringify({distanceKm,fareAmount})}); }
async function payTrip(userId,tripId,driverId,amount){ return request('payments',{method:'POST',body:JSON.stringify({userId,tripId,driverId,amount})}); }
async function sendTripSos(userId,tripId,coords={}){ return request('sos',{method:'POST',body:JSON.stringify({userId,tripId,...coords})}); }
