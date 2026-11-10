import androidx.health.connect.client.permission.HealthPermission;
import androidx.health.connect.client.records.HydrationRecord;

public class TestPerms {
    public static void main(String[] args) {
        System.out.println(HealthPermission.getWritePermission(HydrationRecord.class));
    }
}
