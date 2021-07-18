//
//  RoomViewController.swift
//  MyApp
//
//  Created by Ramachandra on 13/07/21.
//

import Foundation
import UIKit
import IGListDiffKit

class RoomViewController: UIViewController, UIPopoverPresentationControllerDelegate {
    @IBOutlet weak var disableMicButton: CircleToggleButton!
    @IBOutlet weak var disableCameraButton:  CircleToggleButton!
    @IBOutlet weak var leaveButton: UIButton!
    @IBOutlet weak var addButton: UIButton!
    @IBOutlet weak var switchCameraButton: UIButton!
    @IBOutlet weak var roomNameLabel: UILabel!
    @IBOutlet weak var participantCollectionView: UICollectionView!
    @IBOutlet weak var mainVideoView: VideoView!
    @IBOutlet weak var mainIdentityLabel: UILabel!
    @IBOutlet weak var recordingView: UIView!
    var popover: UIPopoverPresentationController?
    private let roomFactory = RoomFactory()
    private var room: Room!
    var videoView: VideoView!
    private var participant: LocalParticipant { room.localParticipant }
    private var shouldRenderVideo = true
    var application: UIApplication!
    var viewModel: RoomViewModel!
    var tokenIS = ""
    override func viewDidLoad() {
        super.viewDidLoad()
//        self.getAcessTokenAPI();
        
        participantCollectionView.delegate = self
        participantCollectionView.register(ParticipantCell.self)

        disableMicButton.didToggle = { [weak self] in self?.viewModel.isMicOn = !$0 }
        disableCameraButton.didToggle = { [weak self] in
            self?.viewModel.isCameraOn = !$0
            self?.updateView()
        }

        viewModel.delegate = self
        viewModel.connect()
    }
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        application.isIdleTimerDisabled = true
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        application.isIdleTimerDisabled = false
    }
    @IBAction func leaveButtonTapped(_ sender: Any) {
        viewModel.disconnect()
        self.dismiss(animated: true, completion: nil)
        //navigationController?.popViewController(animated: true)
    }
    @IBAction func addButtonTapped(_ sender: Any) {
       
        let storyboard: UIStoryboard = UIStoryboard(name: "Video", bundle: nil)
        let vc = storyboard.instantiateViewController(withIdentifier: "popover") as! Popover
        let navController = UINavigationController(rootViewController: vc)
        let rightButton: UIBarButtonItem = UIBarButtonItem(title: "Cancel", style: UIBarButtonItem.Style.done, target: self, action: #selector(RoomViewController.cancelButtonClicked(_:)))
        navController.navigationItem.rightBarButtonItem = rightButton
        
        navController.modalPresentationStyle = UIModalPresentationStyle.popover
        popover = navController.popoverPresentationController!
        popover?.sourceRect = CGRect(x: UIScreen.main.bounds.midX, y: UIScreen.main.bounds.midY, width: 0, height: 0)
        popover?.sourceView = self.view
        popover?.delegate = self
        popover?.permittedArrowDirections = UIPopoverArrowDirection(rawValue: 0)
        vc.preferredContentSize = CGSize(width: 300, height: 300)
        self.present(navController, animated: true, completion: nil)
    }
    @objc func cancelButtonClicked(_ button:UIBarButtonItem!){
        print("Done clicked")
    }
    @IBAction func switchCameraButtonTapped(_ sender: Any) {
        viewModel.cameraPosition = viewModel.cameraPosition == .front ? .back : .front
    }
    
    private func updateView() {
        roomNameLabel.text = viewModel.data.roomName
        disableMicButton.isSelected = !viewModel.isMicOn
        disableCameraButton.isSelected = !viewModel.isCameraOn
        switchCameraButton.isEnabled = viewModel.isCameraOn
        let participant = viewModel.data.mainParticipant
        mainVideoView.configure(config: participant.videoConfig)
        mainIdentityLabel.text = participant.identity
        recordingView.isHidden = !viewModel.data.isRecording
    }
    
    func getAcessTokenAPI()
    {
        let params = ["roomName":"chetan04", "identity":"ram"] as Dictionary<String, String>

        var request = URLRequest(url: URL(string: "https://c9dev2.cloud9download.com:8080/api/Users/getToken")!)
        request.httpMethod = "POST"
        request.httpBody = try? JSONSerialization.data(withJSONObject: params, options: [])
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")

        let session = URLSession.shared
        let task = session.dataTask(with: request, completionHandler: { [self] data, response, error -> Void in
            print(response!)
            do {
                let json = try JSONSerialization.jsonObject(with: data!) as! [String:Any]
                print("json",json)
                let data = json["data"] as! [String:Any]
                print("token is ", data["token"]!)
                self.tokenIS = data["token"] as! String
//                viewModel.delegate = self
//                viewModel.connect()

               

//                updateView()
            } catch {
                print("error")
            }
        })

        task.resume()
    }
    
    private func configureVideoView() {
        let config = VideoView.Config(
            videoTrack: shouldRenderVideo ? participant.cameraTrack : nil,
            shouldMirror: participant.shouldMirrorCameraVideo
        )
        videoView.configure(config: config)
    }
    
    private func resetRoom() {
        self.room = roomFactory.makeRoom()
        participant.isMicOn = true
        participant.isCameraOn = true
    }

}
extension RoomViewController: RoomViewModelDelegate {
    func didConnect() {
        updateView()
    }
    
    func didFailToConnect(error: Error) {
        showError(error: error) { [weak self] in self?.navigationController?.popViewController(animated: true) }
    }
    
    func didDisconnect(error: Error?) {
        if let error = error {
            showError(error: error) { [weak self] in self?.navigationController?.popViewController(animated: true) }
        } else {
            navigationController?.popViewController(animated: true)
        }
    }

    func didUpdateList(diff: ListIndexSetResult) {
        participantCollectionView.performBatchUpdates(
            {
                participantCollectionView.insertItems(at: diff.inserts.indexPaths)
                participantCollectionView.deleteItems(at: diff.deletes.indexPaths)
                diff.moves.forEach { move in
                    participantCollectionView.moveItem(
                        at: IndexPath(item: move.from, section: 0),
                        to: IndexPath(item: move.to, section: 0)
                    )
                }
            },
            completion: nil
        )
    }

    func didUpdateParticipant(at index: Int) {
        guard let cell = participantCollectionView.cellForItem(at: IndexPath(item: index, section: 0)) as? ParticipantCell else { return }
        
        cell.configure(participant: viewModel.data.participants[index])
    }
    
    func didUpdateMainParticipant() {
        updateView()
    }

    func didUpdateRecording() {
        updateView()
    }
}
extension RoomViewController: UICollectionViewDataSource {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        viewModel.data.participants.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: ParticipantCell.identifier, for: indexPath) as! ParticipantCell
        cell.configure(participant: viewModel.data.participants[indexPath.item])
        return cell
    }
}

extension RoomViewController: UICollectionViewDelegate {
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        viewModel.togglePin(at: indexPath.item)
    }
}
