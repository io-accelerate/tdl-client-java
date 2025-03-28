package io.accelerate.client.queue.transport;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * Created by julianghionoiu on 20/06/2015.
 */
public class StringMessage {
    private final Message message;

    public StringMessage(Message message) {
        this.message = message;
    }

    public boolean isValid() {
        return message != null;
    }

    public String getContent() throws JMSException {
        String messageText = "undecoded";

        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            messageText = textMessage.getText();
        }

        return messageText;
    }

    public void acknowledge() throws JMSException {
        message.acknowledge();
    }
}
