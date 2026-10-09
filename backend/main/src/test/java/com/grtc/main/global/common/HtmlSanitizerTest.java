package com.grtc.main.global.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// HtmlSanitizer 가 서식은 남기고 위험한 내용은 지우는지 확인 (DB·스프링 없이 실행되는 단위 테스트)
class HtmlSanitizerTest {

    @Test
    void 값이_없으면_null() {
        assertNull(HtmlSanitizer.clean(null));
        assertNull(HtmlSanitizer.clean("   "));
        assertNull(HtmlSanitizer.clean("<div><br></div>"));
        assertNull(HtmlSanitizer.clean("<script>alert(1)</script>"));
    }

    @Test
    void 에디터_서식은_그대로_남는다() {
        assertEquals("<div><b>굵게</b> 일반</div>", HtmlSanitizer.clean("<div><b>굵게</b> 일반</div>"));
        assertEquals("<ul><li>하나</li><li>둘</li></ul>", HtmlSanitizer.clean("<ul><li>하나</li><li>둘</li></ul>"));
        assertEquals("<ol><li>첫째</li></ol>", HtmlSanitizer.clean("<ol><li>첫째</li></ol>"));
    }

    @Test
    void 링크는_http_https_mailto_만_남고_rel_이_붙는다() {
        String link = HtmlSanitizer.clean("<a href=\"https://example.com/a?b=1\" target=\"_blank\" onclick=\"x()\">링크</a>");
        assertTrue(link.contains("href=\"https://example.com/a?b=1\""), link);
        assertTrue(link.contains("rel=\"nofollow noopener noreferrer\""), link);
        assertFalse(link.contains("onclick"), link);
        assertFalse(link.contains("target"), link);

        String js = HtmlSanitizer.clean("<a href=\"javascript:alert(1)\">누르세요</a>");
        assertFalse(js.toLowerCase().contains("javascript"), js);
        assertFalse(js.contains("href"), js);
        assertTrue(js.contains("누르세요"), js);
    }

    @Test
    void 위험한_태그와_속성은_지워진다() {
        String cleaned = HtmlSanitizer.clean(
                "<div style=\"color:red\" onmouseover=\"x()\">본문<script>alert(1)</script>"
                        + "<img src=x onerror=alert(1)><iframe src=\"https://evil.example\"></iframe></div>");
        assertEquals("<div>본문</div>", cleaned);
    }
}
