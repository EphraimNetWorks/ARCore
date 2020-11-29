package com.networks.arcorefilters

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.AugmentedFaceNode

class MainActivity : AppCompatActivity() {

    private val arFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.fragment) as ArFragment
    }

    private val facePropRecycler by lazy {
        findViewById<RecyclerView>(R.id.face_props_recycler)
    }

    private val bottomSheet by lazy {
        findViewById<LinearLayout>(R.id.bottom_sheet)
    }

    private val takePhotoFab by lazy {
        findViewById<FloatingActionButton>(R.id.take_photo_fab)
    }

    private val photoSaver by lazy{
        PhotoSaver(this)
    }

    private lateinit var selectedModel : FaceProp
    private var selectedFaceRenderable: ModelRenderable? = null
    private var selectedFaceTexture: Texture? = null

    private val adapter = FacePropRecyclerAdapter(
            listOf(
                    FaceProp("Tribal", R.drawable.tribal_face, R.drawable.tribal_uv, PropType.TEXTURE),
                    FaceProp("FeatherTribal", R.drawable.red_indian_tribal_face, R.drawable.red_indian_tribal_uv, PropType.TEXTURE),
                    FaceProp("ColorTribal", R.drawable.color_tribal_face, R.drawable.color_tribal_uv, PropType.TEXTURE),
                    FaceProp("Puppy", R.drawable.puppy_face, R.raw.fox_face, PropType.MODEL),
                    FaceProp("Pirate", R.drawable.pirate_hat, R.raw.pirate_hat, PropType.MODEL),
                    FaceProp("Cowboy", R.drawable.cowboy_hat, R.raw.cowboy_hat, PropType.MODEL),
                    FaceProp("Crown", R.drawable.crown, R.raw.crown, PropType.MODEL),
                    FaceProp("Wizard", R.drawable.wizard_hat, R.raw.wizard_hat, PropType.MODEL),
            ),this)

    private val faceNodeMap = HashMap<AugmentedFace, AugmentedFaceNode>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        facePropRecycler.adapter = adapter
        adapter.apply {
            selectedModel.observe(this@MainActivity, {
                this@MainActivity.selectedModel = it
                when(it.type){
                    PropType.MODEL->{
                        clear()
                        loadModel<ModelRenderable> { modelRenderable ->
                            Log.i("MainActivity","Face prop successfully loaded")
                            selectedFaceRenderable = modelRenderable
                        }
                    }
                    PropType.TEXTURE->{
                        clear()
                        loadModel<Texture> { texture ->
                            Log.i("MainActivity","Face prop successfully loaded")
                            selectedFaceTexture = texture
                        }
                    }
                }

            })
        }

        setupBottomSheet()

        arFragment.arSceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST
        arFragment.arSceneView.scene.addOnUpdateListener {
            if(selectedFaceRenderable!=null || selectedFaceTexture !=null){
                addTrackedFaces()
                removeUntrackedFaces()
            }
        }

        takePhotoFab.setOnClickListener {
            photoSaver.takePhoto(arFragment.arSceneView)
        }

    }

    private fun setupBottomSheet(){
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                BOTTOM_SHEET_PEEK_HEIGHT,
                resources.displayMetrics
        ).toInt()

        bottomSheetBehavior.addBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback(){
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        bottomSheet.bringToFront()
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    }
                }
        )
    }

    private fun addTrackedFaces(){
        val session = arFragment.arSceneView.session ?:return
        val faceList = session.getAllTrackables(AugmentedFace::class.java)
        for (face in faceList){
            if(!faceNodeMap.containsKey(face)){
                AugmentedFaceNode(face).apply {
                    setParent(arFragment.arSceneView.scene)
                    if(selectedFaceRenderable !=null){
                        faceRegionsRenderable = selectedFaceRenderable
                    }else{
                        faceMeshTexture = selectedFaceTexture
                    }
                    faceNodeMap[face] = this
                }
            }
        }
    }

    private fun clear(){
        faceNodeMap.values.forEach {
            it.setParent(null)
            it.faceRegionsRenderable = null
            it.faceMeshTexture = null
        }
        faceNodeMap.clear()
        selectedFaceTexture = null
        selectedFaceRenderable = null
    }

    private fun removeUntrackedFaces(){
        val entries = faceNodeMap.entries
        for (entry in entries){
            val face = entry.key
            if(face.trackingState == TrackingState.STOPPED){
                val faceNode = entry.value
                faceNode.setParent(null)
                entries.remove(entry)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> loadModel(callback:(T)->Unit){
        when(selectedModel.type){
            PropType.TEXTURE->{
                Texture.builder()
                    .setSource(this, selectedModel.modelId)
                    .build()
                    .thenAccept{
                        callback.invoke(it as T)
                    }
                    .exceptionally {
                        Toast.makeText(this, "Error loading face prop", Toast.LENGTH_LONG).show()
                        null
                    }
            }
            PropType.MODEL->{
                ModelRenderable.builder()
                    .setSource(this, selectedModel.modelId)
                    .build()
                    .thenAccept{
                        it.apply {
                            isShadowCaster =true
                            isShadowReceiver =true
                        }
                        callback.invoke(it as T)
                    }
                    .exceptionally {
                        Toast.makeText(this, "Error loading face prop", Toast.LENGTH_LONG).show()
                        null
                    }
            }
        }

    }

    companion object{
        private const val BOTTOM_SHEET_PEEK_HEIGHT = 50F
    }

}