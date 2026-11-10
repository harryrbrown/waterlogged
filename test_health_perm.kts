import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HydrationRecord
println(HealthPermission.getWritePermission(HydrationRecord::class))
println(HealthPermission.getReadPermission(HydrationRecord::class))
