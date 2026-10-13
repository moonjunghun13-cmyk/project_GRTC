// UI preview fixtures only. No server requests or persistent storage.
const notices=[
 ['서비스 안내','민원 서비스 이용 안내','민원 작성 및 답변 확인 화면을 소개하는 예시입니다.'],
 ['이용 안내','첨부파일 등록 시 확인 사항','이미지와 PDF 첨부 안내를 위한 예시 게시글입니다.'],
 ['일반 안내','광주교통공사 시민 의견 접수 안내','시민 의견 접수 안내 화면 확인용 예시입니다.'],
 ['서비스 안내','회원정보 확인 및 로그인 안내','회원정보와 로그인 이용 안내 예시입니다.'],
 ['이용 안내','민원 답변 확인 방법 안내','작성한 민원에서 답변을 확인하는 방법의 예시입니다.'],
 ['일반 안내','공지사항 게시판 이용 안내','공지사항 목록 디자인 확인용 예시입니다.'],
]
const resources=[
 ['이용 안내','민원 서비스 이용 가이드','민원 서비스 안내 자료 예시입니다.','민원서비스_이용가이드_예시.pdf'],
 ['서식','시민 의견 작성 참고 서식','서식 목록 확인을 위한 예시입니다.','시민의견_참고서식_예시.pdf'],
 ['이용 안내','첨부파일 등록 도움말','첨부파일 이용 안내 자료 예시입니다.','첨부파일_도움말_예시.pdf'],
 ['기타','자주 묻는 질문 참고 자료','FAQ 관련 참고 자료 예시입니다.','자주묻는질문_예시.pdf'],
 ['이용 안내','민원 유형별 작성 예시 모음','민원 작성 가이드 참고 자료 예시입니다.','민원작성_예시모음.pdf'],
 ['서식','민원 내용 정리 참고 양식','양식 디자인 확인을 위한 예시입니다.','민원정리_참고양식_예시.pdf'],
]
function entries(rows,kind){return rows.map(([category,title,content,filename],i)=>({id:`example-${kind}-${i+1}`,number:i+1,category,title:`[예시] ${title}`,content,createdAt:`2026-10-${String(12-i).padStart(2,'0')}`,attachments:filename?[{name:filename,previewOnly:true}]:[]}))}
export const boardExamples={notices:entries(notices,'notices'),resources:entries(resources,'resources')}
