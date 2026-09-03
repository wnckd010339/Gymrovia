import fs from "node:fs";
import path from "node:path";

const outputPath = path.resolve(
  "design/generated/UI-04A-member-registration-modal.svg"
);

const svg = `
<svg xmlns="http://www.w3.org/2000/svg" width="1440" height="1024" viewBox="0 0 1440 1024">
  <title>UI-04A 신규 회원 등록 모달</title>
  <defs>
    <filter id="modal-shadow" x="-20%" y="-20%" width="140%" height="150%">
      <feDropShadow dx="0" dy="18" stdDeviation="24" flood-color="#071A14" flood-opacity="0.28"/>
    </filter>
  </defs>

  <!-- 회원 목록 배경 -->
  <rect width="1440" height="1024" fill="#F4F7F6"/>
  <rect width="240" height="1024" fill="#15332C"/>
  <circle cx="42" cy="44" r="20" fill="#C7F04B"/>
  <text x="42" y="51" text-anchor="middle" font-family="Pretendard, Arial" font-size="16" font-weight="900" fill="#15332C">G</text>
  <text x="72" y="51" font-family="Pretendard, Arial" font-size="23" font-weight="800" fill="#FFFFFF">Gymrovia</text>
  <text x="28" y="104" font-family="Pretendard, Arial" font-size="11" font-weight="700" fill="#8FB2A6">관리자</text>
  <rect x="18" y="165" width="204" height="44" rx="12" fill="#245145"/>
  <text x="62" y="193" font-family="Pretendard, Arial" font-size="14" font-weight="700" fill="#FFFFFF">회원 관리</text>
  <text x="280" y="55" font-family="Pretendard, Arial" font-size="26" font-weight="800" fill="#13211D">회원 관리</text>
  <text x="280" y="80" font-family="Pretendard, Arial" font-size="13" fill="#6B7873">등록 회원을 검색하고 상태 및 이용 정보를 관리합니다.</text>
  <rect x="1212" y="34" width="188" height="44" rx="12" fill="#0B6B4F"/>
  <text x="1306" y="62" text-anchor="middle" font-family="Pretendard, Arial" font-size="14" font-weight="700" fill="#FFFFFF">+ 신규 회원</text>
  <line x1="260" y1="104" x2="1410" y2="104" stroke="#DDE6E2"/>
  <rect x="280" y="132" width="1120" height="108" rx="18" fill="#FFFFFF" stroke="#DDE6E2"/>
  <rect x="304" y="174" width="420" height="48" rx="12" fill="#FFFFFF" stroke="#DDE6E2"/>
  <rect x="748" y="174" width="180" height="48" rx="12" fill="#FFFFFF" stroke="#DDE6E2"/>
  <rect x="946" y="174" width="180" height="48" rx="12" fill="#FFFFFF" stroke="#DDE6E2"/>
  <rect x="280" y="310" width="1120" height="506" rx="12" fill="#FFFFFF" stroke="#DDE6E2"/>
  <rect x="280" y="310" width="1120" height="56" rx="12" fill="#EEF3F1"/>
  <g font-family="Pretendard, Arial" font-size="12" font-weight="700" fill="#6B7873">
    <text x="300" y="344">회원</text><text x="470" y="344">연락처</text><text x="640" y="344">회원 상태</text>
    <text x="770" y="344">회원권</text><text x="950" y="344">담당 트레이너</text><text x="1120" y="344">최근 출석</text>
  </g>
  <g stroke="#DDE6E2">
    <line x1="280" y1="424" x2="1400" y2="424"/><line x1="280" y1="482" x2="1400" y2="482"/>
    <line x1="280" y1="540" x2="1400" y2="540"/><line x1="280" y1="598" x2="1400" y2="598"/>
    <line x1="280" y1="656" x2="1400" y2="656"/><line x1="280" y1="714" x2="1400" y2="714"/>
  </g>

  <!-- 딤 배경 -->
  <rect width="1440" height="1024" fill="#071A14" fill-opacity="0.58"/>

  <!-- 신규 회원 등록 모달 -->
  <rect x="370" y="74" width="700" height="810" rx="24" fill="#FFFFFF" filter="url(#modal-shadow)"/>
  <text x="414" y="125" font-family="Pretendard, Arial" font-size="24" font-weight="800" fill="#13211D">신규 회원 등록</text>
  <text x="414" y="151" font-family="Pretendard, Arial" font-size="13" fill="#6B7873">회원 기본 정보와 로그인 계정을 생성합니다.</text>
  <circle cx="1022" cy="118" r="18" fill="#EEF3F1"/>
  <text x="1022" y="124" text-anchor="middle" font-family="Arial" font-size="20" fill="#6B7873">×</text>
  <line x1="370" y1="176" x2="1070" y2="176" stroke="#DDE6E2"/>

  <text x="414" y="219" font-family="Pretendard, Arial" font-size="17" font-weight="800" fill="#13211D">기본 정보</text>
  <text x="414" y="250" font-family="Pretendard, Arial" font-size="12" font-weight="700" fill="#6B7873">이름 *</text>
  <rect x="414" y="262" width="294" height="50" rx="12" fill="#FFFFFF" stroke="#DDE6E2"/>
  <text x="430" y="293" font-family="Pretendard, Arial" font-size="14" fill="#98A49F">회원 이름</text>
  <text x="732" y="250" font-family="Pretendard, Arial" font-size="12" font-weight="700" fill="#6B7873">연락처 *</text>
  <rect x="732" y="262" width="294" height="50" rx="12" fill="#FFFFFF" stroke="#DDE6E2"/>
  <text x="748" y="293" font-family="Pretendard, Arial" font-size="14" fill="#98A49F">010-0000-0000</text>

  <text x="414" y="342" font-family="Pretendard, Arial" font-size="12" font-weight="700" fill="#6B7873">생년월일</text>
  <rect x="414" y="354" width="294" height="50" rx="12" fill="#FFFFFF" stroke="#DDE6E2"/>
  <text x="430" y="385" font-family="Pretendard, Arial" font-size="14" fill="#98A49F">YYYY.MM.DD</text>
  <text x="732" y="342" font-family="Pretendard, Arial" font-size="12" font-weight="700" fill="#6B7873">성별</text>
  <rect x="732" y="354" width="294" height="50" rx="12" fill="#FFFFFF" stroke="#DDE6E2"/>
  <text x="748" y="385" font-family="Pretendard, Arial" font-size="14" fill="#98A49F">선택</text>
  <text x="1000" y="385" text-anchor="middle" font-family="Arial" font-size="14" fill="#6B7873">⌄</text>

  <line x1="414" y1="438" x2="1026" y2="438" stroke="#DDE6E2"/>
  <text x="414" y="478" font-family="Pretendard, Arial" font-size="17" font-weight="800" fill="#13211D">계정 정보</text>
  <text x="414" y="509" font-family="Pretendard, Arial" font-size="12" font-weight="700" fill="#6B7873">로그인 ID *</text>
  <rect x="414" y="521" width="294" height="50" rx="12" fill="#FFFFFF" stroke="#DDE6E2"/>
  <text x="430" y="552" font-family="Pretendard, Arial" font-size="14" fill="#98A49F">영문·숫자 조합</text>
  <text x="732" y="509" font-family="Pretendard, Arial" font-size="12" font-weight="700" fill="#6B7873">초기 비밀번호 *</text>
  <rect x="732" y="521" width="294" height="50" rx="12" fill="#FFFFFF" stroke="#DDE6E2"/>
  <text x="748" y="552" font-family="Pretendard, Arial" font-size="14" fill="#98A49F">8자 이상 입력</text>

  <text x="414" y="612" font-family="Pretendard, Arial" font-size="12" font-weight="700" fill="#6B7873">트레이너 상담 희망</text>
  <rect x="414" y="626" width="612" height="74" rx="14" fill="#F4F7F6"/>
  <circle cx="444" cy="663" r="11" fill="#0B6B4F"/>
  <circle cx="444" cy="663" r="4" fill="#FFFFFF"/>
  <text x="468" y="658" font-family="Pretendard, Arial" font-size="13" font-weight="700" fill="#13211D">상담을 희망합니다</text>
  <text x="468" y="680" font-family="Pretendard, Arial" font-size="11" fill="#6B7873">등록 후 트레이너 배정 대기 목록에 추가됩니다.</text>

  <rect x="414" y="722" width="612" height="54" rx="12" fill="#FFF8E8"/>
  <text x="434" y="745" font-family="Pretendard, Arial" font-size="12" font-weight="700" fill="#9B6B10">등록 안내</text>
  <text x="434" y="764" font-family="Pretendard, Arial" font-size="11" fill="#806B44">초기 비밀번호는 회원에게 안전하게 전달하고 첫 로그인 후 변경하도록 안내합니다.</text>

  <line x1="370" y1="798" x2="1070" y2="798" stroke="#DDE6E2"/>
  <rect x="750" y="818" width="128" height="44" rx="12" fill="#FFFFFF" stroke="#DDE6E2"/>
  <text x="814" y="846" text-anchor="middle" font-family="Pretendard, Arial" font-size="14" font-weight="700" fill="#0B6B4F">취소</text>
  <rect x="894" y="818" width="132" height="44" rx="12" fill="#0B6B4F"/>
  <text x="960" y="846" text-anchor="middle" font-family="Pretendard, Arial" font-size="14" font-weight="700" fill="#FFFFFF">회원 등록</text>
</svg>
`.trim();

fs.writeFileSync(outputPath, svg, "utf8");
console.log(`Generated ${outputPath}`);
