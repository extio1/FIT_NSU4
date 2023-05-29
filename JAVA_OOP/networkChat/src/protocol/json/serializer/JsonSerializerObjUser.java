package protocol.json.serializer;

import com.google.gson.*;
import protocol.ObjectUser;

import java.lang.reflect.Type;

public class JsonSerializerObjUser implements JsonSerializer<ObjectUser> {
    @Override
    public JsonElement serialize(ObjectUser req, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonArray jsonObject = new JsonArray();
        Gson gson = new Gson();

        jsonObject.add(req.getClass().getCanonicalName());
        jsonObject.add(gson.toJson(req));

        return jsonObject;
    }
}
