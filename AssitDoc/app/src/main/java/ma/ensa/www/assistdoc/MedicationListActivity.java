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

    private Button buttonAddMedication;
    private RecyclerView recyclerView;
    private MedicationAdapter medicationAdapter;
    private SearchView searchView;
    private MedicamentDao medicamentDao;
    private List<Medicament> allMedications; // Liste complète des médicaments

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_list);

        initializeViews();
        setupRecyclerView();
        setupSearchView();

        // Récupérer l'accès à la base de données
        AppDatabase db = AppDatabase.getDatabase(this);
        medicamentDao = db.medicamentDao();

        // Observer les changements de données dans la base
        medicamentDao.getAllMedicaments().observe(this, new Observer<List<Medicament>>() {
            @Override
            public void onChanged(List<Medicament> medications) {
                allMedications = medications; // Mise à jour de la liste complète
                medicationAdapter.updateList(medications);
            }
        });

        // Gestion du bouton pour ajouter un médicament
        buttonAddMedication.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivityMedi.class);
            startActivity(intent);
        });
    }

    private void initializeViews() {
        buttonAddMedication = findViewById(R.id.buttonAddMedication);
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
                if (allMedications != null) {
                    filterMedications(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (allMedications != null) {
                    filterMedications(newText);
                }
                return true;
            }
        });
    }

    private void filterMedications(String query) {
        List<Medicament> filteredList = new ArrayList<>();
        for (Medicament medicament : allMedications) {
            if (medicament.getNom().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(medicament);
            }
        }
        medicationAdapter.updateList(filteredList);
    }

    private void confirmDelete(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation de suppression")
                .setMessage("Voulez-vous vraiment supprimer ce médicament ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    Medicament medicamentToDelete = medicationAdapter.getMedications().get(position);
                    deleteMedicament(medicamentToDelete);
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void deleteMedicament(Medicament medicament) {
        // Supprimer le médicament dans un thread séparé
        new Thread(() -> {
            medicamentDao.delete(medicament);
            runOnUiThread(() -> Toast.makeText(this, "Médicament supprimé", Toast.LENGTH_SHORT).show());
        }).start();
    }
}
