package kz.kcell.kwms.model;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpringELTest {
    @Test
    public void test1() {
        FacilityInstance f1 = FacilityInstance.builder()
                .id(1L)
                .params("{}")
                .build();

        FacilityInstance f2 = FacilityInstance.builder()
                .id(2L)
                .params("{}")
                .build();

        Site s1 = Site.builder()
                .id("A")
                .name("Site A")
                .params("{}")
                .facilities(Stream.of(f1, f2).collect(Collectors.toCollection(TreeSet::new)))
                .build();

        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression("#{facilities.![id]}", new TemplateParserContext());


        List result = exp.getValue(s1, List.class);

        Assert.assertTrue(result.contains(f1.getId()));
        Assert.assertTrue(result.contains(f2.getId()));
        Assert.assertTrue(result.size() == s1.getFacilities().size());
    }
}
