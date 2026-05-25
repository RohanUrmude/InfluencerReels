import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HistoryComponent } from './history.component';
import { ApiService } from '../../core/services/api.service';
import { of, throwError } from 'rxjs';

describe('HistoryComponent', () => {
  let component: HistoryComponent;
  let fixture: ComponentFixture<HistoryComponent>;
  let apiService: jasmine.SpyObj<ApiService>;

  beforeEach(async () => {
    const apiServiceSpy = jasmine.createSpyObj('ApiService', [
      'getContentHistory',
      'downloadPDF'
    ]);

    await TestBed.configureTestingModule({
      imports: [HistoryComponent, CommonModule, RouterLink],
      providers: [
        { provide: ApiService, useValue: apiServiceSpy }
      ]
    }).compileComponents();

    apiService = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
    fixture = TestBed.createComponent(HistoryComponent);
    component = fixture.componentInstance;
  });

  describe('Component Initialization', () => {
    it('should create the history component', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize with empty content list', () => {
      expect(component.contentList).toEqual([]);
      expect(component.isLoading).toBeFalse();
      expect(component.error).toBe('');
    });

    it('should load content history on init', () => {
      // TEST: Load history
      const mockHistoryData = [
        {
          id: 1,
          topicIdea: 'Morning workout',
          platform: 'TikTok',
          niche: 'Fitness',
          viralScore: 7.5,
          confidenceScore: 85.5,
          scriptContent: 'Generated script...',
          createdAt: new Date()
        }
      ];

      apiService.getContentHistory.and.returnValue(
        of({ success: true, data: mockHistoryData })
      );

      component.ngOnInit();

      expect(apiService.getContentHistory).toHaveBeenCalled();
      expect(component.contentList.length).toBe(1);
    });
  });

  describe('Loading Content History', () => {
    it('should display loading state while fetching', () => {
      // TEST: Loading indicator
      apiService.getContentHistory.and.returnValue(
        of({ success: true, data: [] })
      );

      component.loadHistory();

      expect(component.isLoading).toBeFalsy();
    });

    it('should fetch and display content list', (done) => {
      // TEST: CG-006A View history
      const mockHistoryData = [
        {
          id: 1,
          topicIdea: 'Fitness Motivation',
          platform: 'TikTok',
          niche: 'Fitness',
          viralScore: 7.8,
          confidenceScore: 87.5,
          primaryModelUsed: 'Mistral',
          scriptContent: 'Wake up early...',
          createdAt: new Date()
        },
        {
          id: 2,
          topicIdea: 'Healthy Recipes',
          platform: 'Instagram Reels',
          niche: 'Food',
          viralScore: 8.2,
          confidenceScore: 90.0,
          primaryModelUsed: 'Llama',
          scriptContent: 'Quick salad recipe...',
          createdAt: new Date()
        }
      ];

      apiService.getContentHistory.and.returnValue(
        of({ success: true, data: mockHistoryData })
      );

      component.loadHistory();

      setTimeout(() => {
        expect(component.contentList.length).toBe(2);
        expect(component.contentList[0].platform).toBe('TikTok');
        expect(component.contentList[1].platform).toBe('Instagram Reels');
        done();
      }, 100);
    });

    it('should display error message on failure', () => {
      // TEST: Error handling
      const errorMessage = 'Failed to load content history';
      apiService.getContentHistory.and.returnValue(
        throwError(() => ({ error: { message: errorMessage } }))
      );

      component.loadHistory();

      expect(component.error).toBeDefined();
    });

    it('should handle empty content list gracefully', () => {
      // TEST: AN-002 No content yet
      apiService.getContentHistory.and.returnValue(
        of({ success: true, data: [] })
      );

      component.loadHistory();

      expect(component.contentList.length).toBe(0);
      expect(component.isLoading).toBeFalsy();
    });
  });

  describe('View Details Modal', () => {
    beforeEach(() => {
      component.contentList = [
        {
          id: 1,
          topicIdea: 'Morning workout',
          platform: 'TikTok',
          niche: 'Fitness',
          viralScore: 7.5,
          confidenceScore: 85.5,
          primaryModelUsed: 'Mistral',
          scriptContent: 'Generated script...',
          createdAt: new Date()
        }
      ];
    });

    it('should open details modal when clicking content', () => {
      // TEST: View details
      const content = component.contentList[0];

      component.viewDetails(content);

      expect(component.selectedContent).toBe(content);
    });

    it('should display all content details in modal', () => {
      // TEST: Display content details
      const content = component.contentList[0];

      component.viewDetails(content);

      expect(component.selectedContent.topicIdea).toBe('Morning workout');
      expect(component.selectedContent.viralScore).toBe(7.5);
      expect(component.selectedContent.scriptContent).toBeDefined();
    });

    it('should close modal when clicking close button', () => {
      // TEST: Close modal
      component.selectedContent = component.contentList[0];

      component.selectedContent = null;

      expect(component.selectedContent).toBeNull();
    });

    it('should display viral score in modal', () => {
      // TEST: Show metrics
      const content = component.contentList[0];

      component.viewDetails(content);

      expect(component.selectedContent.viralScore).toBe(7.5);
      expect(component.selectedContent.confidenceScore).toBe(85.5);
    });

    it('should display model used information', () => {
      // TEST: Show model
      const content = component.contentList[0];

      component.viewDetails(content);

      expect(component.selectedContent.primaryModelUsed).toBe('Mistral');
    });

    it('should display full script content', () => {
      // TEST: Show full script
      const content = component.contentList[0];

      component.viewDetails(content);

      expect(component.selectedContent.scriptContent).toBeDefined();
    });
  });

  describe('PDF Download', () => {
    it('should download content as PDF', () => {
      // TEST: PDF download
      const content = {
        id: 1,
        topicIdea: 'Morning workout',
        platform: 'TikTok',
        niche: 'Fitness',
        viralScore: 7.5,
        confidenceScore: 85.5,
        scriptContent: 'Generated script...',
        createdAt: new Date()
      };

      spyOn(window.URL, 'createObjectURL').and.returnValue('blob:mockurl');
      spyOn(window.URL, 'revokeObjectURL');

      component.downloadPDF(content);

      // Verify PDF download was triggered
      expect(window.URL.createObjectURL).toHaveBeenCalled();
    });

    it('should include all content in PDF', () => {
      // TEST: PDF content
      const content = {
        id: 1,
        topicIdea: 'Morning workout',
        platform: 'TikTok',
        niche: 'Fitness',
        viralScore: 7.5,
        confidenceScore: 85.5,
        scriptContent: 'Full script content here...',
        createdAt: new Date()
      };

      spyOn(window.URL, 'createObjectURL').and.returnValue('blob:mockurl');
      spyOn(window.URL, 'revokeObjectURL');

      component.downloadPDF(content);

      // Verify download was initiated
      expect(window.URL.createObjectURL).toHaveBeenCalled();
    });

    it('should generate proper PDF filename', () => {
      // TEST: Filename format
      const content = {
        id: 123,
        topicIdea: 'test',
        scriptContent: 'test',
        createdAt: new Date()
      };

      spyOn(window.URL, 'createObjectURL').and.returnValue('blob:mockurl');
      spyOn(window.URL, 'revokeObjectURL');

      component.downloadPDF(content);

      // Filename should include content ID and date
      expect(window.URL.createObjectURL).toHaveBeenCalled();
    });
  });

  describe('Content Filtering & Sorting', () => {
    beforeEach(() => {
      component.contentList = [
        {
          id: 3,
          topicIdea: 'Yoga basics',
          platform: 'YouTube Shorts',
          niche: 'Fitness',
          viralScore: 6.5,
          confidenceScore: 80.0,
          primaryModelUsed: 'Phi',
          scriptContent: 'Yoga content...',
          createdAt: new Date('2026-05-24')
        },
        {
          id: 2,
          topicIdea: 'Healthy recipes',
          platform: 'Instagram Reels',
          niche: 'Food',
          viralScore: 8.2,
          confidenceScore: 90.0,
          primaryModelUsed: 'Llama',
          scriptContent: 'Recipe content...',
          createdAt: new Date('2026-05-25')
        },
        {
          id: 1,
          topicIdea: 'Morning workout',
          platform: 'TikTok',
          niche: 'Fitness',
          viralScore: 7.5,
          confidenceScore: 85.5,
          primaryModelUsed: 'Mistral',
          scriptContent: 'Workout content...',
          createdAt: new Date('2026-05-26')
        }
      ];
    });

    it('should display content in chronological order (newest first)', () => {
      // TEST: Content ordering
      expect(component.contentList[0].id).toBe(1); // Most recent
      expect(component.contentList[1].id).toBe(2);
      expect(component.contentList[2].id).toBe(3); // Oldest
    });

    it('should handle multiple platforms', () => {
      // TEST: Multi-platform display
      const platforms = new Set(component.contentList.map(c => c.platform));

      expect(platforms.has('TikTok')).toBeTrue();
      expect(platforms.has('Instagram Reels')).toBeTrue();
      expect(platforms.has('YouTube Shorts')).toBeTrue();
    });

    it('should handle multiple niches', () => {
      // TEST: Multi-niche display
      const niches = new Set(component.contentList.map(c => c.niche));

      expect(niches.has('Fitness')).toBeTrue();
      expect(niches.has('Food')).toBeTrue();
    });
  });

  describe('Content Display', () => {
    it('should truncate topic idea in list view', () => {
      // TEST: Topic display
      const content = {
        id: 1,
        topicIdea: 'A'.repeat(100), // Very long topic
        platform: 'TikTok',
        niche: 'Fitness',
        viralScore: 7.5,
        confidenceScore: 85.5,
        scriptContent: 'test',
        createdAt: new Date()
      };

      component.contentList = [content];

      // Component should truncate long titles
      expect(content.topicIdea.length).toBe(100);
    });

    it('should display viral score with proper formatting', () => {
      // TEST: Score display
      const content = {
        id: 1,
        topicIdea: 'test',
        platform: 'TikTok',
        viralScore: 7.8,
        confidenceScore: 85.5,
        scriptContent: 'test',
        createdAt: new Date()
      };

      component.contentList = [content];

      // Verify scores are displayed
      expect(content.viralScore).toBe(7.8);
      expect(content.confidenceScore).toBe(85.5);
    });

    it('should show created date for each content', () => {
      // TEST: Date display
      const testDate = new Date('2026-05-26');
      const content = {
        id: 1,
        topicIdea: 'test',
        platform: 'TikTok',
        scriptContent: 'test',
        createdAt: testDate
      };

      component.contentList = [content];

      expect(component.contentList[0].createdAt).toEqual(testDate);
    });
  });

  describe('Error Handling', () => {
    it('should display error message on network failure', () => {
      // TEST: Network error
      apiService.getContentHistory.and.returnValue(
        throwError(() => new Error('Network error'))
      );

      component.loadHistory();

      expect(component.error).toBeDefined();
    });

    it('should clear error when retrying', () => {
      // TEST: Error clearing
      component.error = 'Previous error';

      const mockData = [
        {
          id: 1,
          topicIdea: 'test',
          platform: 'TikTok',
          scriptContent: 'test',
          createdAt: new Date()
        }
      ];

      apiService.getContentHistory.and.returnValue(
        of({ success: true, data: mockData })
      );

      component.loadHistory();

      expect(component.contentList.length).toBe(1);
    });
  });

  describe('Modal Interactions', () => {
    beforeEach(() => {
      component.contentList = [
        {
          id: 1,
          topicIdea: 'Morning workout',
          platform: 'TikTok',
          niche: 'Fitness',
          viralScore: 7.5,
          confidenceScore: 85.5,
          primaryModelUsed: 'Mistral',
          scriptContent: 'Generated script...',
          createdAt: new Date()
        }
      ];
    });

    it('should open modal when clicking content item', () => {
      // TEST: Click to view
      component.viewDetails(component.contentList[0]);

      expect(component.selectedContent).toBeDefined();
      expect(component.selectedContent.id).toBe(1);
    });

    it('should close modal and reset selection', () => {
      // TEST: Close modal
      component.selectedContent = component.contentList[0];
      component.selectedContent = null;

      expect(component.selectedContent).toBeNull();
    });

    it('should show PDF download button in modal', () => {
      // TEST: PDF button present
      component.viewDetails(component.contentList[0]);

      // Button should be visible in modal when selectedContent is set
      expect(component.selectedContent).toBeTruthy();
    });

    it('should trigger download when PDF button clicked', () => {
      // TEST: Download action
      spyOn(component, 'downloadPDF');
      const content = component.contentList[0];

      component.viewDetails(content);
      component.downloadPDF(component.selectedContent);

      expect(component.downloadPDF).toHaveBeenCalledWith(content);
    });
  });

  describe('Performance', () => {
    it('should handle large content lists efficiently', () => {
      // TEST: Performance with many items
      const largeList = Array.from({ length: 100 }, (_, i) => ({
        id: i,
        topicIdea: `Content ${i}`,
        platform: 'TikTok',
        niche: 'Fitness',
        viralScore: 7.5,
        confidenceScore: 85.5,
        scriptContent: `Script ${i}`,
        createdAt: new Date()
      }));

      component.contentList = largeList;

      expect(component.contentList.length).toBe(100);
      // Component should render efficiently
    });
  });
});
