package protocol.json.deserializer;

import com.google.gson.*;
import protocol.ObjectServer;
import protocol.ObjectUser;

import java.lang.reflect.Type;

public class JsonDeserializerObjServer implements JsonDeserializer<ObjectServer> {
    @Override
    public ObjectServer deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonArray array = jsonElement.getAsJsonArray();

        Gson gson = new Gson();
        try {
            return (ObjectServer) gson.fromJson(array.get(1).getAsString(), Class.forName(array.get(0).getAsString()));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
