package competition.client;

import javax.jms.BytesMessage;
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

    public String getContent() throws JMSException {
        String messageText = "";
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            messageText = textMessage.getText();
        }
        if (message instanceof BytesMessage) {
            BytesMessage bytesMessage = (BytesMessage) message;
            byte[] body = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(body, (int) bytesMessage.getBodyLength());
            messageText = new String(body);
        }

        return messageText;
    }

    public void acknowledge() throws JMSException {
        message.acknowledge();
    }
}
