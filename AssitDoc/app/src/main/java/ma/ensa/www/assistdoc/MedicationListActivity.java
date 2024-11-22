package ma.ensa.www.assistdoc;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ma.ensa.www.assistdoc.adapter.MedicationAdapter;
import ma.ensa.www.assistdoc.dao.AppDatabase;
import ma.ensa.www.assistdoc.dao.MedicamentDao;
import ma.ensa.www.assistdoc.entities.Medicament;

public class MedicationListActivity extends AppCompatActivity {

    private Button button;
    private RecyclerView recyclerView;
    private MedicationAdapter medicationAdapter;
    private SearchView searchView;
    private MedicamentDao medicamentDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_list);

        initializeViews();
        setupRecyclerView();
        setupSearchView();

        // Initialisation de la base de données Room
        AppDatabase db = AppDatabase.getDatabase(this);
        medicamentDao = db.medicamentDao();

        // Observer les médicaments
        medicamentDao.getAllMedicaments().observe(this, new Observer<List<Medicament>>() {
            @Override
            public void onChanged(List<Medicament> medications) {
                medicationAdapter.updateList(medications);
            }
        });

        button.setOnClickListener(view -> startActivity(new Intent(this, MainActivityPatient.class)));
    }

    private void initializeViews() {
        button = findViewById(R.id.buttonAddMedication);
        recyclerView = findViewById(R.id.recyclerViewMeds);
        searchView = findViewById(R.id.search_button);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        medicationAdapter = new MedicationAdapter(new ArrayList<>(), position -> confirmDelete(position));
        recyclerView.setAdapter(medicationAdapter);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                medicationAdapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                medicationAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private void confirmDelete(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation de suppression")
                .setMessage("Voulez-vous vraiment supprimer ce médicament ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    Medicament medicamentToDelete = medicationAdapter.getMedications().get(position);
                    deleteMedicament(medicamentToDelete);
                    medicationAdapter.removeAt(position);
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void deleteMedicament(Medicament medicament) {
        // Suppression du médicament de la base de données Room
        new Thread(() -> medicamentDao.delete(medicament)).start();

        // Supprimer le médicament du serveur ou effectuer d'autres actions nécessaires
        sendNotificationToReceiver(medicament);
    }

    private void sendNotificationToReceiver(Medicament medicament) {
        // Logique d'envoi de notification
        Toast.makeText(this, "Notification pour " + medicament.getNom(), Toast.LENGTH_SHORT).show();
    }
}
