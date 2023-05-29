package protocol.json.serializer;

import com.google.gson.*;
import protocol.ObjectServer;

import java.lang.reflect.Type;

public class JsonSerializerObjServer implements JsonSerializer<ObjectServer> {
    @Override
    public JsonElement serialize(ObjectServer req, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonArray jsonObject = new JsonArray();
        Gson gson = new Gson();

        jsonObject.add(req.getClass().getCanonicalName());
        jsonObject.add(gson.toJson(req));

        return jsonObject;
    }
}
