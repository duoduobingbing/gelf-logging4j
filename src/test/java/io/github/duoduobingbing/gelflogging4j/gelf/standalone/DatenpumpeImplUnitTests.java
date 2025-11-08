package io.github.duoduobingbing.gelflogging4j.gelf.standalone;

import io.github.duoduobingbing.gelflogging4j.gelf.test.helper.TestAssertions.AssertJAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.duoduobingbing.gelflogging4j.gelf.GelfTestSender;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 */
class DatenpumpeImplUnitTests {

    @BeforeEach
    void before() throws Exception {
        GelfTestSender.getMessages().clear();
    }

    @Test
    void testBean() throws Exception {
        MyBean bean = new MyBean();

        DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost("test:static");

        DatenpumpeImpl datenpumpe = new DatenpumpeImpl(configuration);

        datenpumpe.submit(bean);

        AssertJAssertions.assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        AssertJAssertions.assertThat(gelfMessage.getField("value")).isEqualTo("value field");
        AssertJAssertions.assertThat(gelfMessage.getField("boolean")).isEqualTo("true");
        AssertJAssertions.assertThat(gelfMessage.getField("object")).isNotNull();

        AssertJAssertions.assertThat(gelfMessage.getAdditonalFields()).hasSize(3);

        datenpumpe.close();

        // additional check for NPE
        datenpumpe.close();

    }

    @Test
    void testShoppingCart() throws Exception {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setCartId("the cart id");
        shoppingCart.setAmount(9.27);
        shoppingCart.setCustomerId("the customer id");

        DefaultGelfSenderConfiguration configuration = new DefaultGelfSenderConfiguration();
        configuration.setHost("test:static");

        DatenpumpeImpl datenpumpe = new DatenpumpeImpl(configuration);

        datenpumpe.submit(shoppingCart);

        AssertJAssertions.assertThat(GelfTestSender.getMessages()).hasSize(1);

        GelfMessage gelfMessage = GelfTestSender.getMessages().get(0);

        System.out.println(gelfMessage.toJson());

    }
}
