package org.compacto.integration.springboot;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;

import org.compacto.parser.CompactoParser;
import org.compacto.parser.exceptions.CompactoException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;

public class CompactoMessageConverter extends AbstractGenericHttpMessageConverter<Object> {

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        return readInternal((Class) type, inputMessage);
    }

    @Override
    protected void writeInternal(Object t, Type type, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        try {
            CompactoParser parser = new CompactoParser();
            OutputStream outputStream = outputMessage.getBody();
            String body;
            try {
                body = parser.toCompacto(t, type);
                outputStream.write(body.getBytes());
            } catch (CompactoException e) {
                throw new HttpMessageNotWritableException(e.getMessage());
            } finally {
                outputStream.close();
            }
        } catch (IllegalArgumentException e) {
            throw new HttpMessageNotWritableException(e.getMessage());
        }
    }

    @Override
    protected Object readInternal(Class<? extends Object> clazz,
            HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        StringBuilder sb = new StringBuilder();
        byte[] chunk = new byte[1024];
        while (inputMessage.getBody().read(chunk) > 0) {
            sb.append(new String(chunk));
        }
        CompactoParser parser = new CompactoParser();
        try {
            return parser.fromCompacto(sb.toString(), clazz);
        } catch (CompactoException e) {
            throw new HttpMessageNotReadableException(e.getLocalizedMessage(), inputMessage);
        } finally {
            sb.setLength(0);
        }
    }

    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        boolean debugForce = false;
        if (debugForce || (Object.class).isAssignableFrom((Class) type)) {
            if (mediaType == null)
                return true;

            if (mediaType.getType().equals(MediaType.valueOf("text/compacto").getType())) {
                return true;
            }
        }
        return false;
    }

    public boolean canWrite(@Nullable Type type,
            Class<?> clazz,
            @Nullable MediaType mediaType) {
        try {
            if (mediaType.isCompatibleWith(MediaType.valueOf("text/compacto"))) {
                if (type.getTypeName().equals("?")) {
                    logger.error("Cannot Convert Wildcard (<?>)");
                    return false;
                }

                if ((Object.class).isAssignableFrom(Class.forName(type.getTypeName()))) {// ((Class) type)) {
                    if (mediaType == null)
                        return true;

                    return true;
                }
            }

        } catch (Exception e) {
        }
        return false;
    }
}
