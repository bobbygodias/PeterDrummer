using System.Collections.Generic;
using PeterDrummer.Core;
using PeterDrummer.Data;
using UnityEngine;

namespace PeterDrummer.Rhythm
{
    /// <summary>
    /// Spawna notas com antecedência baseada em distância/velocidade,
    /// garantindo cruzamento no timestamp exato da música.
    /// </summary>
    public class NoteSpawner : MonoBehaviour
    {
        [SerializeField] private SongConductor conductor;
        [SerializeField] private Transform spawnPoint;
        [SerializeField] private Transform hitPoint;
        [SerializeField] private float scrollSpeedUnitsPerSec = 8f;
        [SerializeField] private NoteObject kickPrefab;
        [SerializeField] private NoteObject snarePrefab;
        [SerializeField] private NoteObject hihatPrefab;

        private readonly List<BeatEvent> _events = new();
        private int _nextIndex;

        private float TravelDistance => Mathf.Abs(spawnPoint.position.x - hitPoint.position.x);
        public float TravelTimeSec => TravelDistance / scrollSpeedUnitsPerSec;

        public void SetBeatMap(List<BeatEvent> beatEvents)
        {
            _events.Clear();
            _events.AddRange(beatEvents);
            _events.Sort((a, b) => a.TimeSec.CompareTo(b.TimeSec));
            _nextIndex = 0;
        }

        private void Update()
        {
            if (!conductor.IsPlaying || _events.Count == 0) return;

            double songNow = conductor.CurrentSongTimeSec;

            // Condição de spawn precisa:
            // event.TimeSec - songNow <= TravelTime => nota tem que nascer agora.
            while (_nextIndex < _events.Count)
            {
                BeatEvent evt = _events[_nextIndex];
                double dt = evt.TimeSec - songNow;
                if (dt > TravelTimeSec) break;

                Spawn(evt);
                _nextIndex++;
            }
        }

        private void Spawn(BeatEvent evt)
        {
            NoteObject prefab = evt.Lane switch
            {
                DrumLane.Kick => kickPrefab,
                DrumLane.Snare => snarePrefab,
                _ => hihatPrefab
            };

            NoteObject note = Instantiate(prefab, spawnPoint.position, Quaternion.identity, transform);

            // Momento DSP em que esta nota foi instanciada (referência para animação da posição).
            double spawnDsp = AudioSettings.dspTime;

            note.Initialize(
                evt.Lane,
                evt.TimeSec,
                conductor,
                spawnDsp,
                spawnPoint.position.x,
                hitPoint.position.x,
                scrollSpeedUnitsPerSec);
        }
    }
}
