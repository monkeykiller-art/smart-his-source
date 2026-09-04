const modules=[
  ['总览','⌂'],['患者服务','患'],['临床诊疗','临'],['资源保障','资'],['运营管理','营'],['区域协同','协'],['系统治理','治']
];
const domains=[
  ['患','患者服务','预约、挂号、患者主索引与全景视图'],
  ['临','临床诊疗','门住院工作站、医嘱、病历与危急值'],
  ['资','资源保障','药品、耗材、床位与执行资源协同'],
  ['营','运营管理','收费结算、医保 DRG 与经营分析'],
  ['协','区域协同','转诊、远程医疗与标准化数据交换']
];
const services=['auth','patient','clinical','resource','operations','collaboration','pharma','cdss','drg','emergency','platform'];

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
