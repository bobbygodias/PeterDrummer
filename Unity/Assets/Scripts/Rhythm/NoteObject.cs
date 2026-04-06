using PeterDrummer.Core;
using PeterDrummer.Data;
using UnityEngine;

namespace PeterDrummer.Rhythm
{
    public class NoteObject : MonoBehaviour
    {
        [SerializeField] private double autoDespawnAfterHitSec = 0.2d;

        public DrumLane Lane { get; private set; }
        public double TargetSongTimeSec { get; private set; }

        private SongConductor _conductor;
        private float _hitX;
        private float _directionSign;
        private float _scrollSpeed;

        public void Initialize(
            DrumLane lane,
            double targetSongTimeSec,
            SongConductor conductor,
            float spawnX,
            float hitX,
            float scrollSpeed)
        {
            Lane = lane;
            TargetSongTimeSec = targetSongTimeSec;
            _conductor = conductor;
            _hitX = hitX;
            _scrollSpeed = scrollSpeed;
            _directionSign = Mathf.Sign(spawnX - hitX);
        }

        private void Update()
        {
            if (_conductor == null || !_conductor.IsPlaying) return;

            // Posição absoluta pela diferença entre o tempo atual da música e o tempo alvo da nota.
            // Quando now == target, a nota está exatamente no hitX.
            double now = _conductor.CurrentSongTimeSec;
            double remaining = TargetSongTimeSec - now;
            float x = _hitX + (float)(remaining * _scrollSpeed * _directionSign);

            Vector3 pos = transform.position;
            pos.x = x;
            transform.position = pos;

            if (now > TargetSongTimeSec + autoDespawnAfterHitSec)
            {
                Destroy(gameObject);
            }
        }
    }
}
