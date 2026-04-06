using PeterDrummer.Audio;
using PeterDrummer.Rhythm;
using UnityEngine;

namespace PeterDrummer.Core
{
    /// <summary>
    /// Orquestra pipeline: importar áudio -> carregar -> analisar -> tocar + spawn.
    /// </summary>
    public class GameFlowController : MonoBehaviour
    {
        [SerializeField] private AudioImportService importService;
        [SerializeField] private RuntimeAudioLoader audioLoader;
        [SerializeField] private AudioAnalyzer analyzer;
        [SerializeField] private SongConductor conductor;
        [SerializeField] private NoteSpawner spawner;

        public void SelectAndStartSong()
        {
            importService.PickAudioFile(OnPathPicked, OnError);
        }

        private void OnPathPicked(string path)
        {
            audioLoader.LoadFromAbsolutePath(path, OnAudioLoaded, OnError);
        }

        private void OnAudioLoaded(AudioClip clip)
        {
            var beatEvents = analyzer.Analyze(clip);
            spawner.SetBeatMap(beatEvents);
            conductor.PrepareAndPlay(clip);
            Debug.Log($"Beatmap gerado com {beatEvents.Count} eventos.");
        }

        private void OnError(string error)
        {
            Debug.LogError(error);
        }
    }
}
