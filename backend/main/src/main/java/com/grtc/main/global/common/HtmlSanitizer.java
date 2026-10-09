package com.grtc.main.global.common;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

// 사용자가 보낸 HTML(민원 내용의 서식)에서 허용한 태그만 남기는 유틸
//   - 에디터가 만드는 서식: 굵게(b/strong), 글머리·번호 목록(ul/ol/li), 링크(a), 줄바꿈(br/div/p)
//   - 그 밖의 태그(script, img, iframe, style ...)와 속성(onclick, style ...)은 모두 지운다. (저장형 XSS 방지)
//   - 링크 주소는 http / https / mailto 만 허용한다. (javascript: 주소 차단)
//   ※ 화면에서 v-html 로 그리는 값이므로, 저장하기 전에 반드시 이 메서드를 거쳐야 한다.
public final class HtmlSanitizer {

    private static final Safelist SAFELIST = Safelist.none()
            .addTags("b", "strong", "i", "em", "u", "br", "p", "div", "ul", "ol", "li", "a")
            .addAttributes("a", "href")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addEnforcedAttribute("a", "rel", "nofollow noopener noreferrer");

    // 줄바꿈·들여쓰기를 새로 넣지 않고 보낸 모양 그대로 내보낸다.
    private static final Document.OutputSettings OUTPUT = new Document.OutputSettings().prettyPrint(false);

    private HtmlSanitizer() {
    }

    // 허용 태그만 남긴 HTML 을 돌려준다. 보낸 값이 없거나, 정리하고 나니 글자가 하나도 없으면 null
    public static String clean(String html) {
        if (html == null || html.isBlank()) {
            return null;
        }
        String cleaned = Jsoup.clean(html, "", SAFELIST, OUTPUT);
        if (Jsoup.parseBodyFragment(cleaned).text().isBlank()) {
            return null;
        }
        return cleaned;
    }
}
