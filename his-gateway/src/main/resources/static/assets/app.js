const modules=[['总览','⌂'],['患者服务','患'],['门诊挂号','号'],['临床诊疗','临'],['收费管理','费']];
const domains=[
  ['患','患者服务','患者建档、查询与资料维护'],
  ['号','门诊挂号','科室、医生、排班与挂号处理'],
  ['临','临床诊疗','病历、诊断、医嘱与接诊归档'],
  ['费','收费管理','门诊账单、收款、退费与作废']
];
const services=['auth','patient','clinical','operations'];

document.querySelector('#navigation').innerHTML=modules.map((m,i)=>`<button class="nav-item ${i===0?'active':''}"><b>${m[1]}</b>${m[0]}</button>`).join('');
document.querySelector('#domains').innerHTML=domains.map(d=>`<article class="domain"><div class="domain-icon">${d[0]}</div><h3>${d[1]}</h3><p>${d[2]}</p></article>`).join('');
const serviceBox=document.querySelector('#services');
serviceBox.innerHTML=services.map(s=>`<div class="service"><span>his-${s}</span><span class="status" data-service="${s}">检测中</span></div>`).join('');

function tick(){document.querySelector('#clock').textContent=new Intl.DateTimeFormat('zh-CN',{dateStyle:'medium',timeStyle:'medium',hour12:false}).format(new Date())}
tick();setInterval(tick,1000);

async function refreshHealth(){
  let healthy=0;
  await Promise.all(services.map(async service=>{
    const el=document.querySelector(`[data-service="${service}"]`);
    el.className='status';el.textContent='检测中';
    try{
      const response=await fetch(`/api/${service}/health`,{signal:AbortSignal.timeout(3500)});
      if(!response.ok)throw new Error();
      healthy++;el.className='status online';el.textContent='运行中';
    }catch(_){el.className='status offline';el.textContent='未连接'}
  }));
  document.querySelector('#healthyCount').textContent=healthy;
  document.querySelector('#lastChecked').textContent=`最近检测 ${new Date().toLocaleTimeString('zh-CN',{hour12:false})}`;
}
document.querySelector('#refresh').addEventListener('click',refreshHealth);
refreshHealth();
