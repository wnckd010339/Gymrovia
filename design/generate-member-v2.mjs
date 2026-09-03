import fs from "node:fs";
import path from "node:path";

const outDir = path.resolve("design/generated/member-v2");
fs.mkdirSync(outDir, { recursive: true });

const C = {
  bg:"#F7F8F4", white:"#FFFFFF", ink:"#18322C", muted:"#70817B", line:"#DFE7E2",
  dark:"#153A34", green:"#12A879", mint:"#DDF7ED", lime:"#D9F36A",
  lavender:"#EAE7FF", purple:"#7065C8", peach:"#FFE9D6", orange:"#E78036",
  blue:"#E1F1FF", blueInk:"#3377A5", red:"#D95151", soft:"#EFF3EF"
};
const esc=v=>String(v).replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;");
const rect=(x,y,w,h,fill=C.white,r=20,stroke="none",sw=1)=>`<rect x="${x}" y="${y}" width="${w}" height="${h}" rx="${r}" fill="${fill}" stroke="${stroke}" stroke-width="${sw}"/>`;
const text=(x,y,v,s=14,color=C.ink,weight=400,anchor="start")=>`<text x="${x}" y="${y}" font-family="Pretendard, Inter, Arial, sans-serif" font-size="${s}" font-weight="${weight}" fill="${color}" text-anchor="${anchor}">${esc(v)}</text>`;
const circle=(x,y,r,fill,stroke="none",sw=1)=>`<circle cx="${x}" cy="${y}" r="${r}" fill="${fill}" stroke="${stroke}" stroke-width="${sw}"/>`;
const line=(x1,y1,x2,y2,stroke=C.line,sw=1)=>`<line x1="${x1}" y1="${y1}" x2="${x2}" y2="${y2}" stroke="${stroke}" stroke-width="${sw}"/>`;
const pill=(x,y,w,label,fill=C.mint,color=C.green)=>`${rect(x,y,w,32,fill,16)}${text(x+w/2,y+21,label,12,color,700,"middle")}`;
const btn=(x,y,w,label,kind="primary")=>{
  const fill=kind==="primary"?C.dark:kind==="accent"?C.lime:C.white;
  const color=kind==="primary"?"#FFFFFF":kind==="accent"?C.dark:C.ink;
  return `${rect(x,y,w,48,fill,15,kind==="secondary"?C.line:"none")}${text(x+w/2,y+30,label,14,color,800,"middle")}`;
};
const input=(x,y,w,label,value)=>`${text(x,y,label,12,C.muted,700)}${rect(x,y+12,w,52,C.white,14,C.line)}${text(x+18,y+45,value,14,C.ink,600)}`;
const svg=(body,title)=>`<svg xmlns="http://www.w3.org/2000/svg" width="1440" height="1024" viewBox="0 0 1440 1024"><title>${esc(title)}</title><rect width="1440" height="1024" fill="${C.bg}"/>${body}</svg>`;

function nav(active="홈"){
  const items=["홈","내 회원권","운동","출석","이용 내역"];
  return `${rect(0,0,1440,88,C.white,0)}${circle(54,44,22,C.lime)}${text(54,51,"G",17,C.dark,900,"middle")}${text(88,52,"Gymrovia",24,C.dark,900)}
  ${items.map((v,i)=>`${active===v?rect(370+i*116,25,100,40,C.mint,20):""}${text(420+i*116,51,v,13,active===v?C.green:C.muted,active===v?800:600,"middle")}`).join("")}
  ${circle(1284,44,19,C.soft)}${text(1284,49,"김",12,C.green,800,"middle")}${text(1316,42,"김지훈",13,C.ink,800)}${text(1316,60,"서울 성수점",10,C.muted,500)}
  ${line(0,88,1440,88,C.line)}`;
}
const footer=()=>`${text(54,992,"© 2026 Gymrovia · 회원 전용 포털",11,C.muted,600)}${text(1386,992,"이용약관  ·  개인정보처리방침  ·  고객센터",11,C.muted,600,"end")}`;
const progress=(x,y,w,p,color=C.green)=>`${rect(x,y,w,10,C.soft,5)}${rect(x,y,w*p,10,color,5)}`;
const metric=(x,y,w,label,value,sub,fill=C.white,accent=C.green)=>`${rect(x,y,w,138,fill,22,C.line)}${text(x+24,y+34,label,12,C.muted,700)}${text(x+24,y+78,value,28,C.ink,900)}${text(x+24,y+108,sub,12,accent,700)}`;
const screens=[];

screens.push(["MEMBER-V2-01-home",svg(`
${nav("홈")}
${rect(54,120,1332,258,C.dark,30)}
${circle(1214,244,160,"#214C44")}${circle(1260,208,94,"#2C5B51")}${circle(1324,310,72,C.lime)}
${text(94,176,"GOOD MORNING",11,C.lime,800)}${text(94,224,"지훈님, 오늘도",34,"#FFFFFF",800)}${text(94,268,"건강한 하루를 시작해요.",34,"#FFFFFF",800)}
${text(94,308,"오늘은 하체 근력 루틴이 예정되어 있어요.",14,"#BDD2CB",500)}
${btn(94,324,170,"QR로 입장", "accent")}${btn(278,324,170,"운동 시작","secondary")}
${rect(54,406,1332,152,C.white,24,C.line)}
${metric(78,430,294,"이번 달 출석","14회","지난달보다 3회 더 했어요",C.white,C.green)}
${metric(390,430,294,"회원권","D-12","2026.08.11 만료",C.white,C.orange)}
${metric(702,430,294,"이번 주 운동","4 / 4회","주간 목표 달성",C.white,C.green)}
${metric(1014,430,348,"PT 잔여","5회","다음 수업 금요일 14:00",C.white,C.purple)}
${rect(54,586,850,352,C.white,24,C.line)}${text(82,628,"오늘의 루틴",20,C.ink,900)}${pill(752,606,120,"하체 · 2주차")}
${text(82,674,"하체 근력 강화",25,C.ink,900)}${text(82,702,"김도윤 트레이너 · 예상 64분",13,C.muted,500)}
${[["백 스쿼트","4세트 · 60kg"],["레그 프레스","4세트 · 100kg"],["루마니안 데드리프트","3세트 · 50kg"]].map((a,i)=>`${circle(98,756+i*50,16,i===0?C.lime:C.soft)}${text(98,761+i*50,String(i+1),10,C.dark,800,"middle")}${text(128,752+i*50,a[0],13,C.ink,800)}${text(128,773+i*50,a[1],11,C.muted,500)}`).join("")}
${btn(650,852,222,"오늘 운동 시작","primary")}
${rect(928,586,458,352,C.lavender,24)}${text(958,628,"이번 주 리포트",20,C.ink,900)}${text(958,664,"꾸준함 점수",12,C.muted,700)}${text(958,714,"92",44,C.purple,900)}${text(1020,714,"점",14,C.muted,700)}
${progress(958,748,398,.92,C.purple)}${text(958,784,"지난주보다 8점 올랐어요!",13,C.purple,800)}
${text(958,832,"운동 4회  ·  총 4시간 18분",12,C.ink,600)}${text(958,860,"최고 기록  ·  스쿼트 60kg",12,C.ink,600)}
${footer()}
`,"회원 홈 V2")]);

screens.push(["MEMBER-V2-02-membership",svg(`
${nav("내 회원권")}
${text(54,146,"내 회원권",30,C.ink,900)}${text(54,176,"현재 이용권과 혜택을 한눈에 확인하세요.",14,C.muted,500)}
${rect(54,210,836,348,C.dark,28)}
${text(88,258,"GYMROVIA MEMBERSHIP",12,C.lime,800)}${pill(704,236,144,"정상 이용 중",C.lime,C.dark)}
${text(88,322,"3개월 자유 이용권",32,"#FFFFFF",900)}${text(88,358,"서울 성수점 전 시설 자유 이용",14,"#BDD2CB",500)}
${text(88,418,"2026.05.12",12,"#A7C2B9",600)}${text(856,418,"2026.08.11",12,"#A7C2B9",600,"end")}
${progress(88,438,768,.78,C.lime)}${text(88,486,"남은 기간",12,"#A7C2B9",600)}${text(184,488,"12일",20,"#FFFFFF",900)}
${text(856,488,"78% 이용",13,C.lime,800,"end")}
${rect(914,210,472,348,C.white,28,C.line)}${text(946,256,"이용 요약",20,C.ink,900)}
${[["이번 달 출석","14회"],["잔여 PT","5회"],["정지 가능 기간","14일"],["자동 갱신","사용 안 함"]].map((a,i)=>`${text(946,310+i*50,a[0],13,C.muted,600)}${text(1352,310+i*50,a[1],14,C.ink,800,"end")}`).join("")}
${btn(946,478,190,"센터에 문의","secondary")}
${rect(54,590,642,330,C.white,24,C.line)}${text(84,634,"포함된 혜택",20,C.ink,900)}
${[["✓","헬스장 전 시설 이용"],["✓","락커룸 및 샤워실"],["✓","운동 기록·루틴 관리"],["✓","월간 운동 리포트"]].map((a,i)=>`${circle(96,688+i*52,15,C.mint)}${text(96,693+i*52,"✓",10,C.green,900,"middle")}${text(126,693+i*52,a[1],13,C.ink,700)}`).join("")}
${rect(720,590,666,330,C.white,24,C.line)}${text(750,634,"이전 회원권",20,C.ink,900)}
${[["PT 10회 패키지","2026.05.12 – 09.08","5회 남음"],["1개월 자유 이용권","2026.04.12 – 05.11","만료"]].map((a,i)=>`${rect(750,670+i*104,606,80,i===0?C.lavender:C.soft,16)}${text(774,700+i*104,a[0],14,C.ink,800)}${text(774,726+i*104,a[1],11,C.muted,500)}${pill(1224,691+i*104,104,a[2],i===0?C.white:C.bg,i===0?C.purple:C.muted)}`).join("")}
${footer()}
`,"회원권 V2")]);

screens.push(["MEMBER-V2-03-checkin",svg(`
${nav("출석")}
${text(54,146,"QR 출석",30,C.ink,900)}${text(54,176,"입구의 QR을 스캔하면 바로 입장할 수 있어요.",14,C.muted,500)}
${rect(54,210,850,710,C.white,28,C.line)}
${text(479,258,"QR 스캔",22,C.ink,900,"middle")}${text(479,286,"카메라 화면 중앙에 QR 코드를 맞춰주세요.",13,C.muted,500,"middle")}
${rect(194,330,570,470,C.dark,32)}${rect(250,374,458,382,"#FFFFFF",22)}
${Array.from({length:10},(_,i)=>Array.from({length:10},(_,j)=>((i*3+j*2+i*j)%5<2)?rect(274+j*40,398+i*34,24,24,C.dark,3):"").join("")).join("")}
${rect(274,398,106,106,"none",4,C.green,9)}${rect(578,398,106,106,"none",4,C.green,9)}${rect(274,626,106,106,"none",4,C.green,9)}${line(218,564,740,564,C.lime,4)}
${pill(402,828,154,"카메라 준비 완료")}
${rect(930,210,456,332,C.dark,28)}${text(964,258,"입장 준비 완료",20,"#FFFFFF",900)}${pill(1234,236,116,"이용 가능",C.lime,C.dark)}
${circle(992,322,30,C.lime)}${text(992,329,"김",15,C.dark,900,"middle")}${text(1042,312,"김지훈",17,"#FFFFFF",900)}${text(1042,338,"회원권 D-12",12,"#BDD2CB",500)}
${text(964,396,"오늘 출석",12,"#AAC4BB",600)}${text(1352,396,"기록 없음",13,"#FFFFFF",800,"end")}
${text(964,438,"센터 혼잡도",12,"#AAC4BB",600)}${text(1352,438,"보통 · 31명",13,C.lime,800,"end")}
${btn(964,466,388,"스캔 시작","accent")}
${rect(930,568,456,352,C.peach,28)}${text(964,614,"잠깐, 확인해 주세요",20,C.ink,900)}
${[["1","활성 회원권이 있어야 입장할 수 있어요."],["2","입실 중에는 중복 체크인이 되지 않아요."],["3","운동이 끝나면 꼭 체크아웃해 주세요."]].map((a,i)=>`${circle(978,672+i*70,17,C.white)}${text(978,678+i*70,a[0],11,C.orange,900,"middle")}${text(1010,677+i*70,a[1],12,C.ink,700)}`).join("")}
${footer()}
`,"QR 체크인 V2")]);

screens.push(["MEMBER-V2-04-checkout",svg(`
${nav("출석")}
${rect(54,122,1332,228,C.dark,30)}
${circle(112,236,46,C.lime)}${text(112,245,"✓",25,C.dark,900,"middle")}
${text(184,198,"지금 센터를 이용 중이에요",30,"#FFFFFF",900)}${text(184,236,"오늘 09:42 체크인 · 서울 성수점",14,"#BBD0C9",500)}
${pill(184,266,92,"입실 중",C.lime,C.dark)}
${text(1338,196,"현재 이용 시간",12,"#AAC3BA",600,"end")}${text(1338,246,"01:18:24",38,C.lime,900,"end")}
${rect(54,382,788,538,C.white,26,C.line)}${text(86,426,"오늘의 출석 타임라인",21,C.ink,900)}
${line(134,500,134,770,C.line,4)}
${[["09:42","체크인 완료","QR 인증"],["현재","운동 중","1시간 18분 이용"],["예정","체크아웃","퇴실 시간이 자동 저장됩니다"]].map((a,i)=>`${circle(134,520+i*126,20,i===0?C.green:i===1?C.orange:C.white,i===2?C.line:"none",3)}${i<2?text(134,527+i*126,i===0?"✓":"●",11,"#FFFFFF",900,"middle"):""}${text(184,510+i*126,a[0],12,C.muted,700)}${text(184,540+i*126,a[1],16,C.ink,900)}${text(184,566+i*126,a[2],12,C.muted,500)}`).join("")}
${rect(86,836,724,58,C.blue,16)}${text(110,871,"현재 센터 혼잡도  ·  보통  ·  31명 이용 중",13,C.blueInk,800)}
${rect(870,382,516,538,C.lavender,26)}${text(906,426,"운동을 마치셨나요?",23,C.ink,900)}${text(906,458,"체크아웃하면 오늘 출석이 저장됩니다.",13,C.muted,500)}
${rect(906,508,444,156,C.white,22)}${text(934,548,"오늘의 예상 기록",12,C.muted,700)}${text(934,596,"1시간 18분",30,C.ink,900)}${text(934,630,"09:42 – 11:00",13,C.muted,600)}
${btn(906,712,444,"체크아웃하기","primary")}${btn(906,776,444,"조금 더 운동할게요","secondary")}
${text(1128,866,"퇴실할 때 잊지 말고 눌러주세요!",12,C.purple,800,"middle")}
${footer()}
`,"체크아웃 V2")]);

screens.push(["MEMBER-V2-05-routine",svg(`
${nav("운동")}
${text(54,146,"내 운동 루틴",30,C.ink,900)}${text(54,176,"트레이너가 준비한 이번 주 프로그램이에요.",14,C.muted,500)}
${rect(54,210,1332,160,C.dark,26)}${text(88,256,"김지훈 · 근력 향상",24,"#FFFFFF",900)}${text(88,288,"4주 프로그램 · 현재 2주차 · 담당 김도윤 트레이너",13,"#BBD0C8",500)}
${progress(88,322,866,.5,C.lime)}${text(982,327,"2 / 4주",12,C.lime,800)}
${btn(1160,266,190,"오늘 운동 시작","accent")}
${rect(54,398,900,522,C.white,26,C.line)}${text(86,442,"이번 주 일정",20,C.ink,900)}
${["월 · 하체","화 · 상체","목 · 등/이두","토 · 전신"].map((v,i)=>`${rect(86+i*202,470,184,46,i===0?C.dark:C.soft,15)}${text(178+i*202,499,v,13,i===0?"#FFFFFF":C.muted,800,"middle")}`).join("")}
${[["백 스쿼트","4세트 · 60kg","완료"],["레그 프레스","4세트 · 100kg","완료"],["루마니안 데드리프트","3세트 · 50kg","진행"],["레그 익스텐션","3세트 · 35kg","대기"]].map((a,i)=>`${rect(86,550+i*80,836,64,C.white,16,C.line)}${circle(112,582+i*80,16,i<2?C.green:i===2?C.orange:C.soft)}${text(112,587+i*80,i<2?"✓":String(i+1),10,i<3?"#FFFFFF":C.muted,900,"middle")}${text(146,576+i*80,a[0],14,C.ink,800)}${text(146,598+i*80,a[1],11,C.muted,500)}${pill(814,566+i*80,82,a[2],i<2?C.mint:C.soft,i<2?C.green:C.muted)}`).join("")}
${rect(982,398,404,250,C.peach,26)}${text(1014,442,"트레이너 한마디",19,C.ink,900)}${circle(1036,492,24,C.white)}${text(1036,499,"김",12,C.orange,900,"middle")}${text(1076,486,"김도윤 트레이너",13,C.ink,800)}${text(1076,508,"어제 18:40",10,C.muted,500)}
${text(1014,552,"무릎 정렬과 하강 속도에 집중하고,",12,C.ink,600)}${text(1014,578,"세트 간 휴식은 90초 유지해 주세요.",12,C.ink,600)}
${rect(982,674,404,246,C.lavender,26)}${text(1014,718,"이번 주 달성률",19,C.ink,900)}${text(1014,778,"75%",38,C.purple,900)}${progress(1014,804,340,.75,C.purple)}${text(1014,846,"한 번만 더 운동하면 목표 달성!",13,C.purple,800)}
${footer()}
`,"운동 루틴 V2")]);

screens.push(["MEMBER-V2-06-record",svg(`
${nav("운동")}
${text(54,146,"운동 기록",30,C.ink,900)}${text(54,176,"오늘 수행한 세트와 중량을 바로 기록하세요.",14,C.muted,500)}
${rect(54,210,1332,104,C.dark,24)}${text(88,252,"하체 근력 · 운동 중",20,"#FFFFFF",900)}${pill(284,232,128,"34분 18초",C.lime,C.dark)}${text(88,282,"3 / 5개 운동 완료",12,"#BBD0C8",500)}${progress(1060,256,292,.6,C.lime)}
${rect(54,342,840,578,C.white,26,C.line)}${text(86,388,"루마니안 데드리프트",22,C.ink,900)}${text(86,418,"목표 · 3세트 · 10회 · 50kg",12,C.muted,500)}
${rect(86,452,776,52,C.soft,14)}${text(118,484,"세트",12,C.muted,700)}${text(322,484,"중량",12,C.muted,700)}${text(520,484,"횟수",12,C.muted,700)}${text(742,484,"완료",12,C.muted,700)}
${[["1","50 kg","10회",true],["2","50 kg","10회",true],["3","50 kg","횟수 입력",false]].map((a,i)=>`${text(128,550+i*82,a[0],14,C.ink,900,"middle")}${rect(254,520+i*82,164,50,C.white,14,C.line)}${text(336,551+i*82,a[1],14,C.ink,700,"middle")}${rect(452,520+i*82,164,50,C.white,14,C.line)}${text(534,551+i*82,a[2],14,a[3]?C.ink:C.muted,700,"middle")}${rect(724,529+i*82,32,32,a[3]?C.green:C.white,10,a[3]?C.green:C.line)}${a[3]?text(740,552+i*82,"✓",12,"#FFFFFF",900,"middle"):""}`).join("")}
${rect(86,786,776,64,C.peach,16)}${text(110,814,"메모",11,C.orange,800)}${text(166,814,"허리 중립 유지, 마지막 세트 자극 좋음",12,C.ink,600)}
${btn(86,862,178,"이전 운동","secondary")}${btn(280,862,582,"현재 운동 완료","primary")}
${rect(922,342,464,278,C.lavender,26)}${text(954,386,"오늘 기록",20,C.ink,900)}
${[["운동 시간","34분"],["완료 운동","3 / 5"],["총 볼륨","2,340kg"]].map((a,i)=>`${text(954,440+i*46,a[0],12,C.muted,600)}${text(1350,440+i*46,a[1],14,i===1?C.purple:C.ink,900,"end")}`).join("")}
${rect(922,648,464,272,C.white,26,C.line)}${text(954,692,"다음 운동",20,C.ink,900)}${text(954,742,"레그 익스텐션",18,C.ink,900)}${text(954,770,"3세트 · 12–15회 · 35kg",12,C.muted,500)}${btn(954,820,400,"다음 운동으로","secondary")}
${footer()}
`,"운동 기록 V2")]);

screens.push(["MEMBER-V2-07-payments",svg(`
${nav("이용 내역")}
${text(54,146,"결제 내역",30,C.ink,900)}${text(54,176,"회원권과 PT 상품의 결제·환불 기록이에요.",14,C.muted,500)}
${metric(54,210,410,"총 결제","₩760,000","4건의 결제",C.white,C.green)}
${metric(486,210,410,"총 환불","₩30,000","1건의 부분 환불",C.white,C.red)}
${metric(918,210,468,"실제 이용 금액","₩730,000","정상 처리",C.lavender,C.purple)}
${rect(54,380,1332,540,C.white,26,C.line)}${text(86,426,"최근 결제",20,C.ink,900)}
${pill(1126,404,104,"최근 1년",C.soft,C.muted)}${pill(1244,404,110,"전체 내역",C.mint,C.green)}
${[["2026.07.30","3개월 자유 이용권","카드 결제","₩180,000","완료"],["2026.06.12","PT 10회 패키지","부분 환불","-₩30,000","환불"],["2026.06.11","1개월 자유 이용권","카드 결제","₩80,000","완료"],["2026.05.02","PT 10회 패키지","현금 결제","₩500,000","완료"]].map((a,i)=>`${rect(86,472+i*94,1268,76,i===1?C.peach:C.white,16,i===1?"none":C.line)}${circle(118,510+i*94,18,i===1?C.peach:C.mint)}${text(118,515+i*94,i===1?"↩":"₩",11,i===1?C.orange:C.green,900,"middle")}${text(154,498+i*94,a[1],14,C.ink,800)}${text(154,523+i*94,`${a[0]} · ${a[2]}`,11,C.muted,500)}${text(1120,511+i*94,a[3],15,i===1?C.red:C.ink,900,"end")}${pill(1160,494+i*94,88,a[4],i===1?C.white:C.mint,i===1?C.orange:C.green)}${text(1328,518+i*94,"영수증 ›",11,C.muted,700,"end")}`).join("")}
${rect(86,862,1268,34,C.soft,12)}${text(104,884,"환불은 센터 관리자 확인 후 처리됩니다. 문의 02-1234-5678",11,C.muted,600)}
${footer()}
`,"결제 내역 V2")]);

screens.push(["MEMBER-V2-08-profile",svg(`
${nav("홈")}
${text(54,146,"내 정보",30,C.ink,900)}${text(54,176,"개인정보와 알림·보안 설정을 관리하세요.",14,C.muted,500)}
${rect(54,210,370,710,C.dark,28)}
${circle(239,300,58,C.lime)}${text(239,311,"김",28,C.dark,900,"middle")}${text(239,388,"김지훈",24,"#FFFFFF",900,"middle")}${text(239,416,"jihoon94 · MEMBER",12,"#B7CDC5",600,"middle")}
${pill(184,448,110,"계정 활성",C.lime,C.dark)}
${line(90,510,388,510,"#365D54")}
${[["회원권","D-12"],["이번 달 출석","14회"],["PT 잔여","5회"]].map((a,i)=>`${text(90,560+i*60,a[0],12,"#A9C2B9",600)}${text(388,560+i*60,a[1],14,"#FFFFFF",800,"end")}`).join("")}
${btn(90,762,298,"비밀번호 변경","secondary")}${btn(90,826,298,"로그아웃","secondary")}
${rect(450,210,936,444,C.white,28,C.line)}${text(482,254,"개인 정보",20,C.ink,900)}
${input(482,302,410,"이름","김지훈")}${input(920,302,434,"생년월일","1994.08.17")}
${input(482,394,410,"연락처","010-1234-5678")}${input(920,394,434,"이메일","jihoon@gmail.com")}
${input(482,486,872,"주소","서울특별시 성동구 아차산로 00")}
${btn(1160,584,194,"변경 저장","primary")}
${rect(450,680,936,240,C.white,28,C.line)}${text(482,724,"알림 설정",20,C.ink,900)}
${[["회원권 만료 알림","만료 14일·7일·1일 전 안내",true],["운동 루틴 알림","운동일 오전 9시 안내",true],["이벤트 및 혜택","마케팅 정보 수신",false]].map((a,i)=>`${text(482,776+i*48,a[0],13,C.ink,800)}${text(662,776+i*48,a[1],11,C.muted,500)}${rect(1288,760+i*48,58,30,a[2]?C.green:C.line,15)}${circle(a[2]?1330:1304,775+i*48,11,C.white)}`).join("")}
${footer()}
`,"내 정보 V2")]);

for(const [name,body] of screens) fs.writeFileSync(path.join(outDir,`${name}.svg`),body,"utf8");
console.log(`Generated ${screens.length} member V2 screens in ${outDir}`);
