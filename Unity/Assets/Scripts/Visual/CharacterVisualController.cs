using PeterDrummer.Data;
using PeterDrummer.InputSystem;
using UnityEngine;

namespace PeterDrummer.Visual
{
    /// <summary>
    /// Recebe eventos de input e dispara animações/efeitos no personagem.
    /// </summary>
    public class CharacterVisualController : MonoBehaviour
    {
        [SerializeField] private PlayerInputController inputController;
        [SerializeField] private Animator animator;

        private static readonly int KickHash = Animator.StringToHash("Kick");
        private static readonly int SnareHash = Animator.StringToHash("Snare");
        private static readonly int HiHatHash = Animator.StringToHash("HiHat");

        private void OnEnable()
        {
            inputController.OnLanePlayed += OnLanePlayed;
        }

        private void OnDisable()
        {
            inputController.OnLanePlayed -= OnLanePlayed;
        }

        private void OnLanePlayed(DrumLane lane)
        {
            int trigger = lane switch
            {
                DrumLane.Kick => KickHash,
                DrumLane.Snare => SnareHash,
                _ => HiHatHash
            };

            animator.SetTrigger(trigger);
        }
    }
}
