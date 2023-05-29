package protocol.json.deserializer;

import com.google.gson.*;
import protocol.ObjectUser;

import java.lang.reflect.Type;

public class JsonDeserializerObjUser implements JsonDeserializer<ObjectUser> {
    @Override
    public ObjectUser deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonArray array = jsonElement.getAsJsonArray();

        Gson gson = new Gson();
        try {
            return (ObjectUser) gson.fromJson(array.get(1).getAsString(), Class.forName(array.get(0).getAsString()));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
