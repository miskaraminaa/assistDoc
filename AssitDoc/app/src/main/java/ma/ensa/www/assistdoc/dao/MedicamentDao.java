package ma.ensa.www.assistdoc.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import ma.ensa.www.assistdoc.entities.Medicament;

@Dao
public interface MedicamentDao {

    @Insert
    void insert(Medicament medicament);

    @Update
    void update(Medicament medicament);

    @Delete
    void delete(Medicament medicament);

    @Query("SELECT * FROM medicaments")
    LiveData<List<Medicament>> getAllMedicaments();
}
