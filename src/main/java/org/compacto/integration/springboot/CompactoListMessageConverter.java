package org.compacto.integration.springboot;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.compacto.parser.CompactoParser;
import org.compacto.parser.exceptions.CompactoException;
import org.compacto.parser.model.CompactoSerializable;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;

public class CompactoListMessageConverter extends AbstractGenericHttpMessageConverter<List<CompactoSerializable>> {

    @Override
    protected void writeInternal(List<CompactoSerializable> t, Type type, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        CompactoParser parser = new CompactoParser();
        OutputStream outputStream = outputMessage.getBody();
        String body;
        try {
            body = parser.toCompacto(t, type);
            outputStream.write(body.getBytes());
        } catch (CompactoException e) {
            e.printStackTrace();
        }
        outputStream.close();
    }

    @Override
    public List<CompactoSerializable> read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        return readInternal((Class) type, inputMessage);
    }

    @Override
    protected List<CompactoSerializable> readInternal(Class<? extends List<CompactoSerializable>> clazz,
            HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        throw new UnsupportedOperationException("Unimplemented method 'readInternal'");
    }

    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        return false;
    }

    public boolean canWrite(@Nullable Type type,
            Class<?> clazz,
            @Nullable MediaType mediaType) {
        if (type != null && type instanceof ParameterizedType) {
            if (((ParameterizedType) type).getRawType() == List.class) {
                Type typeArg = ((ParameterizedType) type).getActualTypeArguments()[0];
                // try {
                    // if ((CompactoSerializable.class).isAssignableFrom(Class.forName(typeArg.getTypeName()))) {
                        if (mediaType == null)
                            return true;

                        if (mediaType.getType().equals(MediaType.valueOf("text/compacto").getType())) {
                            return true;
                        }
                    // }
                // } catch (ClassNotFoundException e) {
                //     e.printStackTrace();
                // }
            }
        }
        return false;
    }
}
