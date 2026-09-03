import fs from "node:fs";
import path from "node:path";

const outDir = path.resolve("design/generated/auth-flow");
fs.mkdirSync(outDir, { recursive: true });

const C = {
  bg: "#F5F7F5", white: "#FFFFFF", ink: "#17332C", muted: "#71807B",
  line: "#DDE6E2", dark: "#153A34", green: "#13A879", mint: "#DDF7ED",
  lime: "#D9F36A", blue: "#E8F2FF", red: "#D95151"
};
const esc = value => String(value).replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
const rect = (x, y, w, h, fill = C.white, r = 18, stroke = "none") =>
  `<rect x="${x}" y="${y}" width="${w}" height="${h}" rx="${r}" fill="${fill}" stroke="${stroke}"/>`;
const text = (x, y, value, size = 14, color = C.ink, weight = 500, anchor = "start") =>
  `<text x="${x}" y="${y}" font-family="Pretendard, Inter, Arial, sans-serif" font-size="${size}" font-weight="${weight}" fill="${color}" text-anchor="${anchor}">${esc(value)}</text>`;
const circle = (x, y, r, fill) => `<circle cx="${x}" cy="${y}" r="${r}" fill="${fill}"/>`;
const line = (x1, y1, x2, y2, stroke = C.line) => `<line x1="${x1}" y1="${y1}" x2="${x2}" y2="${y2}" stroke="${stroke}"/>`;
const input = (x, y, w, label, value) =>
  `${text(x, y, label, 12, C.muted, 700)}${rect(x, y + 12, w, 50, C.white, 13, C.line)}${text(x + 16, y + 43, value, 14, value ? C.ink : "#9BA7A3", 600)}`;
const button = (x, y, w, label, kind = "primary") => {
  const fill = kind === "primary" ? C.dark : kind === "google" ? C.white : C.mint;
  const color = kind === "primary" ? C.white : C.ink;
  return `${rect(x, y, w, 50, fill, 14, kind === "google" ? C.line : "none")}${kind === "google" ? circle(x + 28, y + 25, 10, "#4285F4") : ""}${text(x + w / 2, y + 31, label, 14, color, 800, "middle")}`;
};
const shell = (body, title) => `<svg xmlns="http://www.w3.org/2000/svg" width="1440" height="1024" viewBox="0 0 1440 1024">
<title>${esc(title)}</title><rect width="1440" height="1024" fill="${C.bg}"/>${body}</svg>`;
const brand = () => `${circle(64,52,22,C.lime)}${text(64,59,"G",17,C.dark,900,"middle")}${text(98,60,"Gymrovia",24,C.dark,900)}`;
const step = (x, number, label, active) =>
  `${circle(x, 128, 18, active ? C.dark : C.white)}${text(x, 133, number, 11, active ? C.white : C.muted, 900, "middle")}${text(x + 28, 133, label, 12, active ? C.ink : C.muted, 700)}`;

const screens = [];

screens.push(["UI-01-login", shell(`
${rect(0,0,650,1024,C.dark,0)}${circle(72,68,25,C.lime)}${text(72,76,"G",20,C.dark,900,"middle")}${text(112,77,"Gymrovia",29,C.white,900)}
${text(72,252,"내 운동의 흐름을",42,C.white,900)}${text(72,306,"한곳에서 관리하세요.",42,C.lime,900)}
${text(72,358,"회원은 운동과 출석을, 관리자는 센터 운영을 더 편하게.",16,"#BBD0C8",500)}
${rect(72,450,500,212,"#214C44",24)}${text(104,494,"회원 전용 기능",13,C.lime,800)}
${text(104,542,"QR 출석  ·  회원권  ·  운동 루틴",18,C.white,800)}${text(104,584,"운동 기록  ·  결제 내역  ·  내 정보",18,C.white,800)}
${text(104,626,"담당 트레이너는 원하는 회원만 선택할 수 있어요.",12,"#BBD0C8",500)}
${rect(650,0,790,1024,C.bg,0)}${text(824,178,"Gymrovia 시작하기",32,C.ink,900)}${text(824,214,"가입한 방식으로 로그인해 주세요.",14,C.muted,500)}
${input(824,282,440,"로그인 ID","")}${input(824,372,440,"비밀번호","••••••••")}
${button(824,464,440,"로그인")}
${line(824,548,1008,548)}${text(1044,553,"또는",12,C.muted,600,"middle")}${line(1080,548,1264,548)}
${button(824,582,440,"Google로 로그인","google")}
${text(1044,682,"아직 회원이 아니신가요?",13,C.muted,500,"middle")}${button(824,710,440,"회원가입","soft")}
${rect(824,794,440,74,C.blue,14)}${text(846,824,"관리자 계정은 센터에서 발급합니다.",12,C.ink,800)}${text(846,848,"회원은 직접 가입 후 바로 로그인할 수 있어요.",11,C.muted,500)}
`, "AUTH-01 공통 로그인")]);

screens.push(["AUTH-02-signup-choice", shell(`
${brand()}${text(720,132,"회원가입",30,C.ink,900,"middle")}${text(720,164,"원하는 로그인 방식을 선택하세요.",14,C.muted,500,"middle")}
${step(488,1,"방식 선택",true)}${line(584,128,694,128)}${step(720,2,"정보 입력",false)}${line(816,128,926,128)}${step(952,3,"가입 완료",false)}
${rect(338,226,764,610,C.white,28,C.line)}
${text(720,282,"어떻게 로그인할까요?",24,C.ink,900,"middle")}${text(720,314,"선택한 방식은 나중에 계정 설정에서 연동할 수 있어요.",12,C.muted,500,"middle")}
${rect(390,366,660,146,C.white,20,C.line)}${circle(438,414,24,C.mint)}${text(438,421,"ID",11,C.green,900,"middle")}
${text(482,402,"아이디와 비밀번호로 가입",17,C.ink,900)}${text(482,430,"Gymrovia 전용 로그인 정보를 새로 만들어요.",12,C.muted,500)}${text(1014,424,"›",26,C.green,800,"end")}
${rect(390,536,660,146,C.white,20,C.line)}${circle(438,584,24,"#E8F2FF")}${text(438,590,"G",14,"#4285F4",900,"middle")}
${text(482,572,"Google로 계속하기",17,C.ink,900)}${text(482,600,"Google 인증 후 필수 정보만 추가 입력해요.",12,C.muted,500)}${text(1014,594,"›",26,C.green,800,"end")}
${rect(390,710,660,72,C.mint,16)}${text(414,739,"트레이너 배정은 선택 사항입니다.",12,C.green,800)}${text(414,762,"혼자 운동하고 싶다면 ‘희망 안 함’을 선택하면 돼요.",11,C.muted,500)}
${text(720,884,"이미 계정이 있나요?  로그인",13,C.green,800,"middle")}
`, "AUTH-02 가입 방식 선택")]);

screens.push(["UI-02-member-registration", shell(`
${brand()}${step(488,1,"방식 선택",false)}${line(584,128,694,128)}${step(720,2,"정보 입력",true)}${line(816,128,926,128)}${step(952,3,"가입 완료",false)}
${rect(250,176,940,788,C.white,28,C.line)}${text(294,226,"일반 회원가입",26,C.ink,900)}${text(294,256,"Gymrovia 전용 계정과 센터 이용 정보를 입력해 주세요.",13,C.muted,500)}
${input(294,310,408,"로그인 ID","jihoon94")}${input(738,310,408,"이메일","jihoon@gmail.com")}
${input(294,400,408,"비밀번호","••••••••")}${input(738,400,408,"비밀번호 확인","••••••••")}
${input(294,490,408,"이름","김지훈")}${input(738,490,408,"연락처","010-1234-5678")}
${text(294,594,"담당 트레이너",12,C.muted,700)}
${rect(294,612,408,72,C.mint,16)}${circle(322,648,10,C.green)}${text(350,644,"트레이너 상담을 원해요",13,C.ink,800)}${text(350,665,"관리자가 확인 후 배정합니다.",11,C.muted,500)}
${rect(738,612,408,72,C.white,16,C.line)}${circle(766,648,10,C.white)}${text(794,644,"혼자 운동할게요",13,C.ink,800)}${text(794,665,"배정 없이 모든 기본 기능을 이용합니다.",11,C.muted,500)}
${text(294,742,"☑ 이용약관에 동의합니다.     ☑ 개인정보 처리방침에 동의합니다.",12,C.ink,600)}
${button(294,800,852,"가입 완료")}
${text(720,894,"Google로 가입하고 싶다면 이전 단계로 돌아가세요.",11,C.muted,500,"middle")}
`, "AUTH-03 일반 회원가입")]);

screens.push(["UI-02G-google-first-login-profile", shell(`
${brand()}${step(488,1,"Google 인증",false)}${line(584,128,694,128)}${step(720,2,"추가 정보",true)}${line(816,128,926,128)}${step(952,3,"가입 완료",false)}
${rect(310,190,820,728,C.white,28,C.line)}
${circle(720,258,30,"#E8F2FF")}${text(720,267,"G",18,"#4285F4",900,"middle")}
${text(720,318,"Google 인증이 완료됐어요",25,C.ink,900,"middle")}${text(720,350,"센터 이용에 필요한 정보만 추가로 입력해 주세요.",13,C.muted,500,"middle")}
${rect(358,386,724,66,C.blue,16)}${text(382,414,"연결된 계정",11,C.muted,700)}${text(382,437,"jihoon@gmail.com",13,C.ink,800)}
${input(358,506,340,"이름","김지훈")}${input(742,506,340,"연락처","010-1234-5678")}
${text(358,606,"담당 트레이너를 원하시나요?",12,C.muted,700)}
${rect(358,624,340,66,C.mint,16)}${text(382,652,"●  상담을 원해요",13,C.ink,800)}${text(382,674,"관리자가 확인 후 배정",10,C.muted,500)}
${rect(742,624,340,66,C.white,16,C.line)}${text(766,652,"○  혼자 운동할게요",13,C.ink,800)}${text(766,674,"트레이너 없이 이용",10,C.muted,500)}
${text(358,740,"☑ 이용약관 동의     ☑ 개인정보 처리 동의",12,C.ink,600)}
${button(358,790,724,"가입 완료")}
${text(720,872,"Google 비밀번호는 Gymrovia에 저장되지 않습니다.",11,C.muted,500,"middle")}
`, "AUTH-04 Google 추가 정보")]);

const exportedScreens = screens.filter(([name]) => name !== "AUTH-02-signup-choice");

for (const [name, content] of exportedScreens) {
  fs.writeFileSync(path.join(outDir, `${name}.svg`), content, "utf8");
}
fs.writeFileSync(
  path.join(outDir, "manifest.json"),
  JSON.stringify(exportedScreens.map(([name]) => path.join(outDir, `${name}.svg`)), null, 2),
  "utf8"
);
console.log(`Generated ${exportedScreens.length} auth-flow screens in ${outDir}`);
