package src.cordova.plugin.videocall


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import io.ionic.starter.R
import kotlinx.android.synthetic.main.select_participant_dialog.view.*


class CustomDialog(): DialogFragment() {

  companion object{
    lateinit var participantDetails: List<ParticipantDetails>
    fun newInstance(participantDetails: List<ParticipantDetails>?): CustomDialog {
      CustomDialog.participantDetails = participantDetails!!
      return CustomDialog()
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.select_participant_dialog, container)
  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    view.select_recycler.layoutManager = LinearLayoutManager(activity)
    var selectParticipantRecycler = SelectParticipantRecycler(participantDetails, object :
      SelectParticipantRecycler.ParticipantSelectListener {
      override fun onParticipantSelected(position: Int) {
        dismiss()

        Toast.makeText(
          activity,
          "Selected: ${participantDetails[position].name}",
          Toast.LENGTH_LONG
        ).show()
        val intent = Intent("selectedParticipant")
        val b = Bundle()
        b.putString("identity", participantDetails[position].userId)
        intent.putExtras(b)
        LocalBroadcastManager.getInstance(activity!!).sendBroadcastSync(intent)
      }
    })
    if(participantDetails.isEmpty()){
      view.empty.visibility = View.VISIBLE
      view.select_recycler.visibility = View.GONE
    }else {
      view.empty.visibility = View.GONE
      view.select_recycler.visibility = View.VISIBLE
      view.select_recycler.adapter = selectParticipantRecycler
    }
  }

}
